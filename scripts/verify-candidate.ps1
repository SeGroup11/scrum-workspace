#requires -Version 7.4
[CmdletBinding()]
param(
    [Parameter(Mandatory)] [string] $PluginZip,
    [Parameter(Mandatory)] [ValidatePattern('^[a-fA-F0-9]{64}$')] [string] $Sha256,
    [Parameter(Mandatory)] [string] $VerifierJar,
    [string] $Idea = (Join-Path $env:LOCALAPPDATA 'Programs/IntelliJ IDEA'),
    [string] $WebStorm = (Join-Path $env:ProgramFiles 'JetBrains/WebStorm 2024.2.3'),
    [string] $Rider = (Join-Path $env:ProgramFiles 'JetBrains/JetBrains Rider 2024.2.6'),
    [string] $PyCharm = (Join-Path $env:LOCALAPPDATA 'Programs/PyCharm'),
    [string] $Output = (Join-Path $PSScriptRoot '../build/verification')
)
$ErrorActionPreference = 'Stop'
$zip = Get-Item -LiteralPath $PluginZip
$verifier = Get-Item -LiteralPath $VerifierJar
if ((Get-FileHash -LiteralPath $zip.FullName).Hash -ine $Sha256) {
    throw 'Candidate ZIP checksum differs from the handoff.'
}
$targets = [ordered]@{
    'IU-262.10315.125' = $Idea
    'WS-262.10315.144' = $WebStorm
    'RD-262.9437.287' = $Rider
    'PY-262.9437.214' = $PyCharm
}
foreach ($target in $targets.GetEnumerator()) {
    $info = Get-Content -LiteralPath (Join-Path $target.Value 'product-info.json') -Raw | ConvertFrom-Json
    $build = "$($info.productCode)-$($info.buildNumber)"
    if ($target.Key -ne $build) { throw "Expected $($target.Key), found $build at $($target.Value)." }
}

$run = New-Item -ItemType Directory -Force -Path (Join-Path $Output ((Get-Date -Format 'yyyyMMdd-HHmmssfff') + '-' + $Sha256.Substring(0,12)))
$candidate = Join-Path $run.FullName $zip.Name
Copy-Item -LiteralPath $zip.FullName -Destination $candidate
if ((Get-FileHash -LiteralPath $candidate).Hash -ine $Sha256) { throw 'Candidate changed while copying.' }
$reports = Join-Path $run.FullName 'reports'
$log = Join-Path $run.FullName 'verifier.log'
$cache = [IO.Path]::GetFullPath((Join-Path $Output 'cache'))
$java = Join-Path $Idea 'jbr/bin/java.exe'
$installations = @($targets.Values)
& $java "-Dplugin.verifier.home.dir=$cache" -jar $verifier.FullName check-plugin $candidate @installations -offline -verification-reports-dir $reports *> $log
$exitCode = $LASTEXITCODE
$results = foreach ($build in $targets.Keys) {
    $verdicts = @(Get-ChildItem -LiteralPath (Join-Path $reports $build) -Recurse -File -Filter verification-verdict.txt -ErrorAction SilentlyContinue)
    $verdict = if ($verdicts.Count -eq 1) { (Get-Content -LiteralPath $verdicts[0].FullName -Raw).Trim() } else { 'Missing or ambiguous verdict' }
    [pscustomobject]@{ build = $build; verdict = $verdict; compatible = $verdict -match '^Compatible(?:[.\s]|$)' }
}
$summary = [ordered]@{
    candidate = $zip.Name
    sha256 = $Sha256.ToLowerInvariant()
    verifier = $verifier.Name
    verifierSha256 = (Get-FileHash -LiteralPath $verifier.FullName).Hash
    os = [System.Environment]::OSVersion.VersionString
    exitCode = $exitCode
    results = @($results)
}
$summary | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $run.FullName 'result.json') -Encoding utf8NoBOM
$results | Format-Table
Write-Output $run.FullName
if ($exitCode -ne 0 -or @($results | Where-Object { !$_.compatible }).Count) {
    throw "See verifier.log and per-IDE reports under $($run.FullName)."
}
