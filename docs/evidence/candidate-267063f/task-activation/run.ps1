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
foreach ($sub in 'classes/META-INF','plugins/observer/lib','config','system','logs','Task-QA/.idea') {
    New-Item -ItemType Directory -Force -Path "$dir/$sub" | Out-Null
}
'<project version="4"/>' | Set-Content -LiteralPath "$dir/Task-QA/.idea/misc.xml" -Encoding utf8NoBOM
@'
<idea-plugin>
  <id>team.segroup11.test.coldtask</id><name>Group 11 native Tasks probe</name><version>1</version><vendor>CS5351 Group 11</vendor>
  <depends>com.intellij.modules.platform</depends><depends>com.intellij.modules.tasks</depends><depends>team.segroup11.issue-review</depends>
  <extensions defaultExtensionNs="com.intellij"><appStarter id="group11TaskProbe" implementation="fixture.TaskProbe"/></extensions>
</idea-plugin>
'@ | Set-Content -LiteralPath "$dir/classes/META-INF/plugin.xml" -Encoding utf8NoBOM
[IO.Compression.ZipFile]::ExtractToDirectory($archive.FullName, "$dir/plugins")
& "$PyCharm/jbr/bin/javac.exe" -encoding UTF-8 -cp "$PyCharm/lib/*;$PyCharm/plugins/tasks/lib/*;$dir/plugins/github-issue-review/lib/*" -d "$dir/classes" "$PSScriptRoot/TaskProbe.java"
if ($LASTEXITCODE -ne 0) { throw 'Observer compilation failed.' }
[IO.Compression.ZipFile]::CreateFromDirectory("$dir/classes", "$dir/plugins/observer/lib/observer.jar")
$root = $dir.Replace('\','/')
$props = @("idea.config.path=$root/config","idea.system.path=$root/system","idea.plugins.path=$root/plugins","idea.log.path=$root/logs","review.probe.path=$root")
$props | Set-Content -LiteralPath "$dir/isolated.properties" -Encoding utf8NoBOM
(@('-Djava.awt.headless=true') + @($props | ForEach-Object { "-D$_" })) | Set-Content -LiteralPath "$dir/isolated.vmoptions" -Encoding utf8NoBOM
$envForTest = @{ PYCHARM_VM_OPTIONS="$dir/isolated.vmoptions"; PYCHARM_PROPERTIES="$dir/isolated.properties" }
Write-Host $dir
$p = Start-Process -FilePath "$PyCharm/bin/pycharm64.exe" -ArgumentList 'group11TaskProbe' -PassThru -WorkingDirectory $dir -WindowStyle Hidden -Environment $envForTest -RedirectStandardOutput "$dir/stdout.log" -RedirectStandardError "$dir/stderr.log"
if (!$p.WaitForExit(60000)) { throw 'Task observer exceeded 60 seconds.' }
$result = @(Get-Content -LiteralPath "$dir/result.txt")
$result
if ($p.ExitCode -ne 0 -or $result[-1] -ne 'PASS') { throw 'At least one real task transition failed.' }
