#requires -Version 7.4
param(
    [Parameter(Mandatory)] [string] $Zip,
    [Parameter(Mandatory)] [string] $PyCharm,
    [Parameter(Mandatory)] [string] $Output
)
$ErrorActionPreference = 'Stop'
$expected = '71c549a8203edb80060d37411ff344a32b10cec534982121163fe920c52b4cd4'
$archive = Get-Item -LiteralPath $Zip
if ((Get-FileHash -LiteralPath $archive.FullName).Hash -ine $expected) { throw 'Unexpected ZIP hash.' }
$info = Get-Content -LiteralPath "$PyCharm/product-info.json" -Raw | ConvertFrom-Json
if ("$($info.productCode)-$($info.buildNumber)" -ne 'PY-262.9437.214') { throw 'Unexpected IDE build.' }
$dir = (New-Item -ItemType Directory -Path (Join-Path $Output (Get-Date -Format 'yyyyMMdd-HHmmssfff')) -Force).FullName
foreach ($sub in 'classes/META-INF','plugins/observer/lib','config','system','logs') {
    New-Item -ItemType Directory -Force -Path "$dir/$sub" | Out-Null
}
@'
<idea-plugin>
  <id>team.segroup11.test.settings</id><name>Group 11 settings probe</name><version>1</version><vendor>CS5351 Group 11</vendor>
  <depends>com.intellij.modules.platform</depends>
  <depends>team.segroup11.issue-review</depends>
  <extensions defaultExtensionNs="com.intellij"><appStarter id="group11SettingsProbe" implementation="fixture.SettingsProbe"/></extensions>
</idea-plugin>
'@ | Set-Content -LiteralPath "$dir/classes/META-INF/plugin.xml" -Encoding utf8NoBOM
& "$PyCharm/jbr/bin/javac.exe" -encoding UTF-8 -cp "$PyCharm/lib/*" -d "$dir/classes" "$PSScriptRoot/SettingsProbe.java"
if ($LASTEXITCODE -ne 0) { throw 'Observer compilation failed.' }
[IO.Compression.ZipFile]::CreateFromDirectory("$dir/classes", "$dir/plugins/observer/lib/observer.jar")
[IO.Compression.ZipFile]::ExtractToDirectory($archive.FullName, "$dir/plugins")
$root = $dir.Replace('\','/')
$props = @("idea.config.path=$root/config","idea.system.path=$root/system","idea.plugins.path=$root/plugins","idea.log.path=$root/logs","review.probe.path=$root")
$props | Set-Content -LiteralPath "$dir/isolated.properties" -Encoding utf8NoBOM
(@('-Djava.awt.headless=true') + @($props | ForEach-Object { "-D$_" })) | Set-Content -LiteralPath "$dir/isolated.vmoptions" -Encoding utf8NoBOM
$envForTest = @{ PYCHARM_VM_OPTIONS="$dir/isolated.vmoptions"; PYCHARM_PROPERTIES="$dir/isolated.properties" }
$p = Start-Process -FilePath "$PyCharm/bin/pycharm64.exe" -ArgumentList 'group11SettingsProbe' -PassThru -WorkingDirectory $dir -WindowStyle Hidden -Environment $envForTest -RedirectStandardOutput "$dir/stdout.log" -RedirectStandardError "$dir/stderr.log"
Write-Host $dir
if (!$p.WaitForExit(60000)) { throw 'Observer exceeded 60 seconds.' }
Get-Content -LiteralPath "$dir/result.txt"
if ($p.ExitCode -ne 0) { throw 'Observer failed.' }
