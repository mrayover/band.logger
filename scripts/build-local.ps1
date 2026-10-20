$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot
$tools = Join-Path $root '.tools'
New-Item -ItemType Directory -Force $tools | Out-Null
$jdk = Get-ChildItem "$tools\jdk" -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $jdk) {
    $archive = Join-Path $tools 'jdk.zip'
    Invoke-WebRequest 'https://api.adoptium.net/v3/binary/version/jdk-21.0.12.1+1/windows/x64/jdk/hotspot/normal/eclipse' -OutFile $archive
    Expand-Archive $archive "$tools\jdk" -Force
    $jdk = Get-ChildItem "$tools\jdk" -Directory | Select-Object -First 1
}
$priorJava = $env:JAVA_HOME
try {
    $env:JAVA_HOME = $jdk.FullName
    & "$PSScriptRoot\package-windows.ps1"
} finally { $env:JAVA_HOME = $priorJava }