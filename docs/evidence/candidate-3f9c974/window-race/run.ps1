#requires -Version 7.4
param(
    [Parameter(Mandatory)] [string] $Zip,
    [Parameter(Mandatory)] [ValidatePattern('^[a-fA-F0-9]{64}$')] [string] $Sha256,
    [Parameter(Mandatory)] [string] $PyCharm,
    [Parameter(Mandatory)] [string] $Output
)
$ErrorActionPreference = 'Stop'
$archive = Get-Item -LiteralPath $Zip
if ((Get-FileHash -LiteralPath $archive.FullName).Hash -ine $Sha256) { throw 'Unexpected ZIP hash.' }
$info = Get-Content -LiteralPath "$PyCharm/product-info.json" -Raw | ConvertFrom-Json
if ("$($info.productCode)-$($info.buildNumber)" -ne 'PY-262.9437.214') { throw 'Unexpected IDE build.' }
$dir = (New-Item -ItemType Directory -Path (Join-Path $Output (Get-Date -Format 'yyyyMMdd-HHmmssfff')) -Force).FullName
foreach ($sub in 'classes/META-INF','plugins/observer/lib','config','system','logs','Window-QA/.idea') {
    New-Item -ItemType Directory -Force -Path "$dir/$sub" | Out-Null
}
'<project version="4"/>' | Set-Content -LiteralPath "$dir/Window-QA/.idea/misc.xml" -Encoding utf8NoBOM
@'
<idea-plugin>
  <id>team.segroup11.test.windowrace</id><name>Group 11 window race probe</name><version>1</version><vendor>CS5351 Group 11</vendor>
  <depends>com.intellij.modules.platform</depends><depends>com.intellij.modules.tasks</depends><depends>team.segroup11.issue-review</depends><depends>org.jetbrains.plugins.github</depends>
  <extensions defaultExtensionNs="com.intellij"><appStarter id="group11WindowProbe" implementation="fixture.WindowProbe"/></extensions>
</idea-plugin>
'@ | Set-Content -LiteralPath "$dir/classes/META-INF/plugin.xml" -Encoding utf8NoBOM
[IO.Compression.ZipFile]::ExtractToDirectory($archive.FullName, "$dir/plugins")
& "$PyCharm/jbr/bin/javac.exe" -encoding UTF-8 -cp "$PyCharm/lib/*;$PyCharm/plugins/tasks/lib/*;$PyCharm/plugins/vcs-github/lib/*;$PyCharm/plugins/vcs-github/lib/modules/*;$dir/plugins/github-issue-review/lib/*" -d "$dir/classes" "$PSScriptRoot/WindowProbe.java"
if ($LASTEXITCODE -ne 0) { throw 'Observer compilation failed.' }
[IO.Compression.ZipFile]::CreateFromDirectory("$dir/classes", "$dir/plugins/observer/lib/observer.jar")
$root = $dir.Replace('\','/')
$props = @("idea.config.path=$root/config","idea.system.path=$root/system","idea.plugins.path=$root/plugins","idea.log.path=$root/logs","review.probe.path=$root")
$props | Set-Content -LiteralPath "$dir/isolated.properties" -Encoding utf8NoBOM
(@('-Djava.awt.headless=true','-Dhttp.proxyHost=127.0.0.1','-Dhttp.proxyPort=1','-Dhttps.proxyHost=127.0.0.1','-Dhttps.proxyPort=1') + @($props | ForEach-Object { "-D$_" })) | Set-Content -LiteralPath "$dir/isolated.vmoptions" -Encoding utf8NoBOM
$envForTest = @{ PYCHARM_VM_OPTIONS="$dir/isolated.vmoptions"; PYCHARM_PROPERTIES="$dir/isolated.properties" }
Write-Host $dir
$p = Start-Process -FilePath "$PyCharm/bin/pycharm64.exe" -ArgumentList 'group11WindowProbe' -PassThru -WorkingDirectory $dir -WindowStyle Hidden -Environment $envForTest -RedirectStandardOutput "$dir/stdout.log" -RedirectStandardError "$dir/stderr.log"
if (!$p.WaitForExit(60000)) {
    # This handle belongs to the isolated observer launched on the preceding line.
    $p.Kill()
    $p.WaitForExit()
    throw 'Window observer exceeded 60 seconds; preserved its isolated logs.'
}
$result = @(Get-Content -LiteralPath "$dir/result.txt")
$result
if ($p.ExitCode -ne 0 -or $result[-1] -ne 'PASS') { throw 'At least one window race condition failed.' }
