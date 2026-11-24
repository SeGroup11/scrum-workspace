#requires -Version 7.4
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string] $PluginZip,
    [Parameter(Mandatory)] [ValidatePattern('^[a-fA-F0-9]{64}$')] [string] $Sha256,
    [string] $Idea = (Join-Path $env:LOCALAPPDATA 'Programs/IntelliJ IDEA'),
    [string] $WebStorm = (Join-Path $env:ProgramFiles 'JetBrains/WebStorm 2024.2.3'),
    [string] $Rider = (Join-Path $env:ProgramFiles 'JetBrains/JetBrains Rider 2024.2.6'),
    [string] $PyCharm = (Join-Path $env:LOCALAPPDATA 'Programs/PyCharm'),
    [hashtable] $TestProfiles = @{},
    [string] $Output = (Join-Path $PSScriptRoot '../build/model-runtime')
)
$ErrorActionPreference = 'Stop'
$zip = Get-Item -LiteralPath $PluginZip
if ((Get-FileHash -LiteralPath $zip.FullName).Hash -ine $Sha256) { throw 'Unexpected candidate checksum.' }
$run = (New-Item -ItemType Directory -Force -Path (Join-Path $Output (Get-Date -Format 'yyyyMMdd-HHmmssfff'))).FullName
$classes = Join-Path $run 'observer-classes'
New-Item -ItemType Directory -Path "$classes/META-INF" -Force | Out-Null
Copy-Item -LiteralPath "$PSScriptRoot/runtime-probe/plugin.xml" -Destination "$classes/META-INF"
& "$Idea/jbr/bin/javac.exe" -encoding UTF-8 -cp "$Idea/lib/*" -d $classes "$PSScriptRoot/runtime-probe/Probe.java"
if ($LASTEXITCODE -ne 0) { throw 'Runtime observer compilation failed.' }
$observer = Join-Path $run 'observer.jar'
[IO.Compression.ZipFile]::CreateFromDirectory($classes, $observer)
$fixtures = Join-Path $PSScriptRoot '../src/test/resources/model'
$issue = Get-Content -LiteralPath "$fixtures/csv-input.json" -Raw | ConvertFrom-Json
$report = Get-Content -LiteralPath "$fixtures/csv-report.json" -Raw
$response = @{
    id = 'probe'; object = 'chat.completion'; created = 0; model = 'fixture'
    choices = @(@{ index = 0; finish_reason = 'stop'; message = @{ role = 'assistant'; content = $report } })
} | ConvertTo-Json -Depth 6 -Compress

$targets = [ordered]@{
    'IU-262.10315.125' = $Idea; 'WS-262.10315.144' = $WebStorm
    'RD-262.9437.287' = $Rider; 'PY-262.9437.214' = $PyCharm
}
$results = foreach ($target in $targets.GetEnumerator()) {
    $dir = Join-Path $run $target.Key
    try {
        $info = Get-Content -LiteralPath (Join-Path $target.Value 'product-info.json') -Raw | ConvertFrom-Json
        if ("$($info.productCode)-$($info.buildNumber)" -ne $target.Key) { throw 'Installed IDE build mismatch.' }
        foreach ($sub in 'plugins/observer/lib','config','system','logs') {
            New-Item -ItemType Directory -Force -Path (Join-Path $dir $sub) | Out-Null
        }
        Copy-Item -LiteralPath $observer -Destination "$dir/plugins/observer/lib"
        if ((Get-FileHash -LiteralPath $zip.FullName).Hash -ine $Sha256) { throw 'Candidate ZIP changed.' }
        [IO.Compression.ZipFile]::ExtractToDirectory($zip.FullName, "$dir/plugins")
        [IO.File]::WriteAllText("$dir/title.txt", $issue.title)
        [IO.File]::WriteAllText("$dir/body.txt", $issue.body)
        [IO.File]::WriteAllText("$dir/response.json", $response)
        $root = $dir.Replace('\','/')
        $config = "$root/config"
        if ($TestProfiles.ContainsKey($info.productCode)) {
            $config = (Get-Item -LiteralPath $TestProfiles[$info.productCode]).FullName.Replace('\','/')
        }
        $settings = @("idea.config.path=$config", "idea.system.path=$root/system",
            "idea.plugins.path=$root/plugins", "idea.log.path=$root/logs", "review.probe.path=$root")
        $settings | Set-Content -LiteralPath "$dir/isolated.properties" -Encoding utf8NoBOM
        (@('-Djava.awt.headless=true') + @($settings | ForEach-Object { "-D$_" })) |
            Set-Content -LiteralPath "$dir/isolated.vmoptions" -Encoding utf8NoBOM
        $environment = @{
            "$($info.envVarBaseName)_VM_OPTIONS" = "$dir/isolated.vmoptions"
            "$($info.envVarBaseName)_PROPERTIES" = "$dir/isolated.properties"
        }
        $launch = $info.launch | Where-Object os -eq 'Windows' | Select-Object -First 1
        $exe = Join-Path $target.Value $launch.launcherPath
        $process = Start-Process -FilePath $exe -ArgumentList 'group11RuntimeProbe' -PassThru -WindowStyle Hidden -WorkingDirectory $dir -Environment $environment -RedirectStandardOutput "$dir/stdout.log" -RedirectStandardError "$dir/stderr.log"
        if (!$process.WaitForExit(60000)) { throw "Observer exceeded 60 seconds (PID $($process.Id))." }
        if (!(Test-Path -LiteralPath "$dir/result.txt")) {
            if (Select-String -LiteralPath "$dir/stdout.log","$dir/stderr.log" -SimpleMatch 'No valid license found' -Quiet) {
                throw 'No valid license found in the test profile; the IDE exited before running the observer.'
            }
            throw "IDE exited before running the observer (exit $($process.ExitCode)); inspect stderr.log."
        }
        $record = @(Get-Content -LiteralPath "$dir/result.txt")
        if ($process.ExitCode -ne 0 -or $record[-1] -ne 'PASS') { throw 'Inspect result.txt and the IDE log.' }
        $requests = @(Get-ChildItem -Path $dir -File -Filter 'request-*.json' | Sort-Object Name)
        if ($requests.Count -ne 2) { throw 'Expected exactly two HTTP requests.' }
        $formats = foreach ($request in $requests) {
            $payload = Get-Content $request.FullName -Raw | ConvertFrom-Json
            $inputFields = $payload.messages[1].content | ConvertFrom-Json -AsHashtable
            if ($inputFields.Count -ne 2 -or $inputFields.title -cne $issue.title -or $inputFields.body -cne $issue.body) {
                throw 'User message fields differ from the original fixture.'
            }
            $payload.response_format.type
        }
        if (($formats -join ',') -ne 'json_object,json_schema') { throw 'Unexpected SDK response formats.' }
        [pscustomobject]@{ build = $target.Key; passed = $true; requests = 2; evidence = "$($target.Key)/result.txt" }
    } catch {
        [pscustomobject]@{ build = $target.Key; passed = $false; error = $_.Exception.Message }
    }
}
[ordered]@{ candidate = $zip.Name; sha256 = $Sha256; coverage = 'Headless IDE, packaged SDK, loopback model'; results = $results } |
    ConvertTo-Json -Depth 5 | Set-Content -LiteralPath "$run/result.json" -Encoding utf8NoBOM
$results | Format-Table
Write-Output $run
if (@($results | Where-Object { !$_.passed }).Count) { throw 'One or more runtime probes failed.' }
