#Requires -Version 7.4
[CmdletBinding()]
param(
    [Parameter(Mandatory, Position = 0)]
    [ValidateSet('backlog', 'planner', 'standup', 'dev', 'review', 'qa', 'release', 'retro', 'off')]
    [string]$Role
)

# Invoke this script in the PowerShell window where you will run gh.
$savedName = 'SeGroup11GhAppOriginalToken'
$saved = Get-Variable -Name $savedName -Scope Global -ErrorAction SilentlyContinue
if ($Role -eq 'off') {
    if ($null -ne $saved) {
        if ($null -eq $saved.Value) {
            Remove-Item Env:GH_TOKEN -ErrorAction SilentlyContinue
        } else {
            $env:GH_TOKEN = $saved.Value
        }
        Remove-Variable -Name $savedName -Scope Global
    }
    Write-Host 'Previous gh authentication restored.'
    return
}

# GitHub assigns these two IDs when the organization registers and installs each App.
$apps = @{
    backlog = @{ AppId = 5898817; InstallationId = 185253998 }
    planner = @{ AppId = 5898855; InstallationId = 185255022 }
    standup = @{ AppId = 5898861; InstallationId = 185255091 }
    dev     = @{ AppId = 5898863; InstallationId = 185255123 }
    review  = @{ AppId = 5898864; InstallationId = 185255151 }
    qa      = @{ AppId = 5898907; InstallationId = 185255278 }
    release = @{ AppId = 5898921; InstallationId = 185255457 }
    retro   = @{ AppId = 5898925; InstallationId = 185255561 }
}

$Role = $Role.ToLowerInvariant()
$app = $apps[$Role]
if ($app.AppId -le 0 -or $app.InstallationId -le 0) {
    throw "Set the actual AppId and InstallationId for '$Role' in the script after installation."
}
$keyFile = Join-Path (Split-Path $PSScriptRoot -Parent) ".github-app-keys/$Role.pem"
if (-not (Test-Path -LiteralPath $keyFile -PathType Leaf)) {
    throw "Private key file missing for '$Role': $keyFile"
}

function ConvertTo-Base64Url([byte[]]$Bytes) {
    [Convert]::ToBase64String($Bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

$now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$utf8 = [Text.Encoding]::UTF8
$claims = @{ iss = [string]$app.AppId; iat = $now - 60; exp = $now + 540 } | ConvertTo-Json -Compress
$header = ConvertTo-Base64Url ($utf8.GetBytes('{"alg":"RS256","typ":"JWT"}'))
$payload = ConvertTo-Base64Url ($utf8.GetBytes($claims))
$signingInput = "$header.$payload"
$rsa = [Security.Cryptography.RSA]::Create()
try {
    $rsa.ImportFromPem([IO.File]::ReadAllText($keyFile))
    $signature = $rsa.SignData($utf8.GetBytes($signingInput), 'SHA256', [Security.Cryptography.RSASignaturePadding]::Pkcs1)
} catch {
    throw "Cannot sign with the private key for '$Role'. Verify the PEM file."
} finally {
    $rsa.Dispose()
}

$headers = @{
    Authorization = "Bearer $signingInput.$(ConvertTo-Base64Url $signature)"
    Accept = 'application/vnd.github+json'
}
$uri = "https://api.github.com/app/installations/$($app.InstallationId)/access_tokens"
try {
    $response = Invoke-RestMethod -Method Post -Uri $uri -Headers $headers -MaximumRedirection 0 -ErrorAction Stop -Verbose:$false -Debug:$false
} catch {
    $status = $_.Exception.Response.StatusCode
    if ($null -ne $status) { throw "GitHub rejected the '$Role' token request (HTTP $([int]$status))." }
    throw "Cannot reach GitHub to get an installation token for '$Role'."
}
if ([string]::IsNullOrWhiteSpace($response.token)) {
    throw "GitHub returned no installation token for '$Role'."
}

if ($null -eq $saved) {
    Set-Variable -Name $savedName -Scope Global -Value ([Environment]::GetEnvironmentVariable('GH_TOKEN'))
}
$env:GH_TOKEN = $response.token
Write-Host "gh identity: $Role"
