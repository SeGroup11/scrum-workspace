#requires -Version 7.4
param([Parameter(Mandatory)][string]$IdeaHome, [Parameter(Mandatory)][string]$Zip, [Parameter(Mandatory)][string]$PlatformHome, [Parameter(Mandatory)][string]$Sha256, [Parameter(Mandatory)][string]$Output)
$ErrorActionPreference = 'Stop'
$archive = (Get-Item -LiteralPath $Zip).FullName
if((Get-FileHash $archive).Hash -ine $Sha256){ throw 'Candidate ZIP digest differs' }
$work = (New-Item -ItemType Directory -Path $Output).FullName
[IO.Compression.ZipFile]::ExtractToDirectory($archive,"$work/package")
$product = @(Get-ChildItem "$work/package/github-issue-review/lib/github-issue-review-*.jar" | Where-Object Name -notmatch 'searchableOptions')
if($product.Count -ne 1){throw 'Ambiguous plugin JAR'}
$kotlin = "$IdeaHome/plugins/Kotlin/kotlinc/lib"
$dirs = @("$work/package/github-issue-review/lib","$PlatformHome/lib","$PlatformHome/plugins/vcs-github/lib","$PlatformHome/plugins/vcs-git/lib","$PlatformHome/plugins/tasks/lib")
$libs = @("$kotlin/kotlin-stdlib.jar","$kotlin/kotlin-test.jar","$kotlin/kotlinx-coroutines-core-jvm.jar") + @($dirs | ForEach-Object { Get-ChildItem -LiteralPath $_ -Filter '*.jar' -File | ForEach-Object FullName })
$classpath = ($libs -join ';').Replace('\','/')
function Invoke-JavaFile([string[]]$Arguments,[string]$Name){
    $path="$work/$Name.args"
    $Arguments | ForEach-Object {'"'+$_.Replace('\','/')+'"'} | Set-Content $path -Encoding utf8NoBOM
    & "$IdeaHome/jbr/bin/java.exe" "@$path" *> "$work/$Name.txt"
    Get-Content "$work/$Name.txt" -Tail 15
    if($LASTEXITCODE -ne 0){throw "$Name failed"}
}
Invoke-JavaFile @('-Xmx1g','-cp',"$kotlin/*",'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-jvm-target','21',("-Xfriend-paths="+$product[0].FullName),'-classpath',$classpath,'-d',"$work/probe.jar","$PSScriptRoot/CommentRecoveryProbe.kt") 'compile'
Invoke-JavaFile @('-Djava.awt.headless=true','-cp',"$work/probe.jar;$classpath",'team.segroup11.review.github.CommentRecoveryProbeKt') 'result'
