#requires -Version 7.4
param(
    [Parameter(Mandatory)][ValidateSet('idea','webstorm','rider','pycharm')][string]$Product,
    [Parameter(Mandatory)][int]$ProductProcessId,
    [Parameter(Mandatory)][string]$Jbr,
    [Parameter(Mandatory)][string]$Output
)
$ErrorActionPreference='Stop'
$p=Get-Process -Id $ProductProcessId
if($p.ProcessName -ine ($Product+'64')){throw 'Unexpected product process'}
$outDir=(New-Item -ItemType Directory -Path $Output -Force).FullName
$file=Join-Path $outDir "$Product-restore-$ProductProcessId.jfr"
if(Test-Path $file){throw 'Existing trace; use a new output directory.'}
$check = & "$Jbr/bin/jcmd.exe" $ProductProcessId JFR.check name=Group11Restore
if($LASTEXITCODE -ne 0 -or ($check -join ' ') -notmatch 'Group11Restore.*running'){throw 'No running startup recording'}
& "$Jbr/bin/jcmd.exe" $ProductProcessId JFR.stop name=Group11Restore ("filename="+$file)
if($LASTEXITCODE -ne 0 -or !(Test-Path $file)){throw 'Recording was not saved'}
$raw = & "$Jbr/bin/jfr.exe" print --json --events jdk.MethodTrace $file
if($LASTEXITCODE -ne 0){throw 'Trace could not be decoded'}
$events = ($raw -join [Environment]::NewLine | ConvertFrom-Json).recording.events
$methods = @($events | Where-Object { $_ -and $_.values.method.type.name -like 'team/segroup11/review/*' } | ForEach-Object {
    [ordered]@{at=$_.values.startTime;method=$_.values.method.name;class=$_.values.method.type.name}
})
$count = [ordered]@{}
foreach($name in 'connect','detail','postComment','evaluate'){$count[$name]=@($methods | Where-Object {$_.method -eq $name}).Count}
$r=[ordered]@{product=$Product;pid=$ProductProcessId;processStartUtc=$p.StartTime.ToUniversalTime().ToString('o');observedUtc=[DateTimeOffset]::UtcNow.ToString('o');recordingSha256=(Get-FileHash $file).Hash.ToLowerInvariant();counts=$count;methods=$methods}
$r | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $outDir "$Product-restart.json") -Encoding utf8NoBOM
$count | ConvertTo-Json -Compress
if(!$count.connect -or !$count.detail){throw 'No positive connection and detail control in this startup window'}
if($count.postComment -or $count.evaluate){throw 'A comment or model call was observed during recovery'}

