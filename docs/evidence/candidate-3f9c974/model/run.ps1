#requires -Version 7.4
param(
    [Parameter(Mandatory)] [string] $Zip,
    [Parameter(Mandatory)] [string] $IdeaHome,
    [Parameter(Mandatory)] [string] $Output,
    [string] $Python = 'python'
)
$ErrorActionPreference = 'Stop'
$archive = Get-Item -LiteralPath $Zip
$hash = (Get-FileHash -LiteralPath $archive.FullName).Hash.ToLowerInvariant()
$expected = Get-Content -LiteralPath "$PSScriptRoot/semantic-replay.json" -Raw | ConvertFrom-Json
if ($hash -cne $expected.zipSha256) { throw 'The ZIP digest differs from the recorded candidate.' }
$directory = New-Item -ItemType Directory -Path $Output -ErrorAction Stop
$extract = Join-Path $directory.FullName 'package'
[IO.Compression.ZipFile]::ExtractToDirectory($archive.FullName, $extract)
$libraries = Join-Path $extract 'github-issue-review/lib/*'
$classpath = "$libraries;$IdeaHome/plugins/Kotlin/kotlinc/lib/kotlin-stdlib.jar;$IdeaHome/lib/*"
$repo = (Resolve-Path -LiteralPath "$PSScriptRoot/../../../..").Path
$start = [Diagnostics.ProcessStartInfo]::new((Get-Command $Python).Source)
$start.UseShellExecute = $false
$start.CreateNoWindow = $true
$start.RedirectStandardOutput = $true
$start.ArgumentList.Add((Join-Path $repo 'src/test/resources/model/mock_provider.py'))
$start.ArgumentList.Add('--port')
$start.ArgumentList.Add('0')
$server = [Diagnostics.Process]::Start($start)
try {
    $header = $server.StandardOutput.ReadLineAsync().WaitAsync([TimeSpan]::FromSeconds(15)).GetAwaiter().GetResult()
    if ($header -notmatch '^Base URL (http://127\.0\.0\.1:\d+/v1)$') { throw 'The loopback server did not start.' }
    $baseUrl = $Matches[1]
    $resultPath = Join-Path $directory.FullName 'semantic-replay.json'
    & "$IdeaHome/jbr/bin/java.exe" -cp $classpath "$PSScriptRoot/ReplayIssues.java" $baseUrl "$PSScriptRoot/issues.json" "$PSScriptRoot/semantic-replay.json" $resultPath $hash
    if ($LASTEXITCODE -ne 0) { throw 'Replay failed. See the report in the output directory.' }
    $counts = 1..2 | ForEach-Object {
        $line = $server.StandardOutput.ReadLineAsync().WaitAsync([TimeSpan]::FromSeconds(5)).GetAwaiter().GetResult()
        if ($line -notmatch "^calls=$_ case=success elapsed_ms=\d+$") { throw "Unexpected request count or case: $line" }
        $line
    }
    $counts | Set-Content -LiteralPath (Join-Path $directory.FullName 'loopback-calls.log') -Encoding utf8
    $counts
} finally {
    if (!$server.HasExited) { $server.Kill(); $server.WaitForExit() }
    $server.Dispose()
}
