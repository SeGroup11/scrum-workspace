#requires -Version 7.4
param(
    [Parameter(Mandatory)][ValidateSet('idea','webstorm','rider','pycharm')][string]$Product,
    [Parameter(Mandatory)][string]$Executable,
    [Parameter(Mandatory)][string]$ProjectPath,
    [string]$ProfileProperties,
    [string]$ProfileVmOptions,
    [int]$PreviousProcessId
)
$ErrorActionPreference = 'Stop'
$binary = (Get-Item -LiteralPath $Executable).FullName
$project = (Get-Item -LiteralPath $ProjectPath).FullName
if (([IO.Path]::GetFileNameWithoutExtension($binary) -ine ($Product + '64'))) { throw 'Unexpected product executable' }
$active = Get-Process -Name ($Product + '64') -ErrorAction SilentlyContinue | Where-Object { $_.Path -eq $binary }
if ($PreviousProcessId -and ($active | Where-Object Id -eq $PreviousProcessId)) { throw 'The previous test process is still running.' }
if ($active -and !$ProfileProperties) { throw 'The normal test instance must finish closing before this recorded restart.' }
$config = (Resolve-Path "$PSScriptRoot/gateway.jfc").Path
$envForLaunch = @{}
# The launcher uses JNI_CreateJavaVM. The diagnostic option applies to this child process only.
$option = '"-XX:StartFlightRecording=name=Group11Restore,settings=' + $config + '"'
$envForLaunch['JAVA_TOOL_OPTIONS'] = ("$env:JAVA_TOOL_OPTIONS " + $option).Trim()
if ($ProfileProperties) { $envForLaunch[$Product.ToUpperInvariant() + '_PROPERTIES'] = (Resolve-Path $ProfileProperties).Path }
if ($ProfileVmOptions) { $envForLaunch[$Product.ToUpperInvariant() + '_VM_OPTIONS'] = (Resolve-Path $ProfileVmOptions).Path }
$p = Start-Process -FilePath $binary -ArgumentList ('"' + $project + '"') -WorkingDirectory $project -Environment $envForLaunch -WindowStyle Normal -PassThru
[ordered]@{product=$Product;process=$p.Id;startedUtc=[DateTimeOffset]::UtcNow.ToString('o')} | ConvertTo-Json

