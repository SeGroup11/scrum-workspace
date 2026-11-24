#requires -Version 7.4
param(
    [Parameter(Mandatory)] [string] $Zip,
    [Parameter(Mandatory)] [string] $PyCharm,
    [Parameter(Mandatory)] [string] $Output,
    [ValidatePattern('^[a-fA-F0-9]{64}$')] [string] $Sha256 = '71c549a8203edb80060d37411ff344a32b10cec534982121163fe920c52b4cd4'
)
$ErrorActionPreference = 'Stop'
$archive = Get-Item -LiteralPath $Zip
if ((Get-FileHash -LiteralPath $archive.FullName).Hash -ine $Sha256) { throw 'Unexpected candidate ZIP hash.' }
$info = Get-Content -LiteralPath "$PyCharm/product-info.json" -Raw | ConvertFrom-Json
if ("$($info.productCode)-$($info.buildNumber)" -ne 'PY-262.9437.214') { throw 'Unexpected IDE build.' }
$dir = (New-Item -ItemType Directory -Path (Join-Path $Output (Get-Date -Format 'yyyyMMdd-HHmmssfff')) -Force).FullName
foreach ($sub in 'classes/META-INF','plugins/observer/lib','config','system','logs','QA-Project/.idea') {
    New-Item -ItemType Directory -Force -Path "$dir/$sub" | Out-Null
}
$fixture = "$PSScriptRoot/../../../../src/test/resources/model"
$issue = Get-Content -LiteralPath "$fixture/csv-input.json" -Raw | ConvertFrom-Json
$report = Get-Content -LiteralPath "$fixture/csv-report.json" -Raw
$response = @{ id='state'; object='chat.completion'; created=0; model='fixture'; choices=@(@{index=0; finish_reason='stop'; message=@{role='assistant';content=$report}}) } | ConvertTo-Json -Depth 6 -Compress
[IO.File]::WriteAllText("$dir/title.txt", $issue.title)
[IO.File]::WriteAllText("$dir/body.txt", $issue.body)
[IO.File]::WriteAllText("$dir/response.json", $response)
'<project version="4"/>' | Set-Content -LiteralPath "$dir/QA-Project/.idea/misc.xml" -Encoding utf8NoBOM
@'
<idea-plugin>
  <id>team.segroup11.test.restart</id><name>Group 11 restart probe</name><version>1</version><vendor>CS5351 Group 11</vendor>
  <depends>com.intellij.modules.platform</depends>
  <depends>com.intellij.modules.tasks</depends>
  <depends>team.segroup11.issue-review</depends>
  <extensions defaultExtensionNs="com.intellij"><appStarter id="group11StateProbe" implementation="fixture.StateProbe"/></extensions>
</idea-plugin>
'@ | Set-Content -LiteralPath "$dir/classes/META-INF/plugin.xml" -Encoding utf8NoBOM
[IO.Compression.ZipFile]::ExtractToDirectory($archive.FullName, "$dir/plugins")
& "$PyCharm/jbr/bin/javac.exe" -encoding UTF-8 -cp "$PyCharm/lib/*;$dir/plugins/github-issue-review/lib/*" -d "$dir/classes" "$PSScriptRoot/StateProbe.java"
if ($LASTEXITCODE -ne 0) { throw 'Observer compilation failed.' }
[IO.Compression.ZipFile]::CreateFromDirectory("$dir/classes", "$dir/plugins/observer/lib/observer.jar")
$root = $dir.Replace('\','/')
Write-Host $dir
foreach ($phase in 'write','read') {
    $props = @("idea.config.path=$root/config","idea.system.path=$root/system","idea.plugins.path=$root/plugins","idea.log.path=$root/logs","review.probe.path=$root","review.probe.phase=$phase")
    $props | Set-Content -LiteralPath "$dir/isolated.properties" -Encoding utf8NoBOM
    (@('-Djava.awt.headless=true') + @($props | ForEach-Object { "-D$_" })) | Set-Content -LiteralPath "$dir/isolated.vmoptions" -Encoding utf8NoBOM
    $envForTest = @{ PYCHARM_VM_OPTIONS="$dir/isolated.vmoptions"; PYCHARM_PROPERTIES="$dir/isolated.properties" }
    $p = Start-Process -FilePath "$PyCharm/bin/pycharm64.exe" -ArgumentList 'group11StateProbe' -PassThru -WorkingDirectory $dir -WindowStyle Hidden -Environment $envForTest -RedirectStandardOutput "$dir/$phase-stdout.log" -RedirectStandardError "$dir/$phase-stderr.log"
    if (!$p.WaitForExit(75000)) { throw "Native shutdown exceeded 75 seconds ($phase)." }
    $record = Get-Content -LiteralPath "$dir/$phase.txt"
    $record
    if ($p.ExitCode -ne 0 -or $record[-1] -ne 'PASS') { throw "Phase failed ($phase)." }
    if ($phase -eq 'write') {
        $workspace = Get-Content -LiteralPath "$dir/QA-Project/.idea/workspace.xml" -Raw
        if ($workspace -notmatch 'Group11IssueReview') { throw 'Platform workspace.xml lacks the plugin component.' }
        [xml]$doc = $workspace
        $doc.SelectSingleNode("/project/component[@name='Group11IssueReview']").OuterXml | Set-Content -LiteralPath "$dir/project-component.xml" -Encoding utf8NoBOM
    }
}
