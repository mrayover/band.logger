param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot
. "$PSScriptRoot/build-version.ps1"
$version = Get-BandLoggerVersion $root
# jpackage recreates the image: never rebuild it while that exact image is running.
$imagePath = [IO.Path]::GetFullPath((Join-Path $root 'app/build/compose/binaries/main/app/band.logger')) + [IO.Path]::DirectorySeparatorChar
Get-Process -Name 'band.logger' -ErrorAction SilentlyContinue | ForEach-Object {
    $runningPath = $_.Path
    if ($runningPath -and $runningPath.StartsWith($imagePath, [StringComparison]::OrdinalIgnoreCase)) { throw 'Close the app-image executable before packaging. Versioned extracted builds may stay open.' }
}
if (Test-Path (Join-Path $root "dist\band.logger-$version-windows-x64.zip")) { throw "Version $version ZIP already exists. Preserve it and choose a new version before packaging." }
Push-Location $root
try {
    if (-not $SkipBuild) {
        & .\gradlew.bat :app:desktopTest :app:createDistributable
        if ($LASTEXITCODE -ne 0) { throw 'Compilation, tests or packaging failed.' }
    }
    $image = Join-Path $root 'app\build\compose\binaries\main\app\band.logger'
    $exe = Join-Path $image 'band.logger.exe'
    if (-not (Test-Path $exe)) { throw "Missing executable: $exe" }
    $smokeDir = Join-Path $root ('app\build\package-smoke-' + [guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Force $smokeDir | Out-Null
    $previousOptions = $env:JAVA_TOOL_OPTIONS
    $env:JAVA_TOOL_OPTIONS = '-Dbandlogger.dataDir="' + $smokeDir + '"'
    try {
        $process = Start-Process -FilePath $exe -ArgumentList '--smoke-test' -PassThru -WindowStyle Hidden
        if (-not $process.WaitForExit(60000)) { $process.Kill(); throw 'Packaged smoke timed out.' }
        if ($process.ExitCode -ne 0 -or -not (Test-Path "$smokeDir\smoke-passed.txt")) { throw 'Packaged executable smoke test failed.' }
        $restart = Start-Process -FilePath $exe -ArgumentList '--event-restart-check' -PassThru -WindowStyle Hidden
        if (-not $restart.WaitForExit(60000)) { $restart.Kill(); throw 'Event restart smoke timed out.' }
        if ($restart.ExitCode -ne 0 -or -not (Test-Path "$smokeDir\event-restart-passed.txt")) { throw 'Packaged event restart smoke failed.' }
    } finally { $env:JAVA_TOOL_OPTIONS = $previousOptions }
    'Extract all files and run band.logger.exe with app/ and runtime/ alongside it. The bundled executable passed its smoke test during this build. See MANUAL_TESTING.md for owner review.' | Set-Content (Join-Path $image 'LAUNCH_STATUS.txt')
    Copy-Item 'MANUAL_TESTING.md' $image
    Copy-Item 'THIRD_PARTY_NOTICES.md' $image
    Copy-Item 'app\src\desktopMain\resources\fonts\OFL.txt' (Join-Path $image 'Lato-OFL.txt')
    New-Item -ItemType Directory -Force 'dist' | Out-Null
    Compress-Archive -Path $image -DestinationPath "dist\band.logger-$version-windows-x64.zip" -Force
    # Test the actual archive in a fresh path containing spaces, with no developer Java on PATH.
    $extracted = Join-Path $root ('app\build\ZIP review ' + [guid]::NewGuid().ToString('N'))
    Expand-Archive "dist\band.logger-$version-windows-x64.zip" $extracted
    $previousJava = $env:JAVA_HOME
    $previousPath = $env:PATH
    $previousOptions = $env:JAVA_TOOL_OPTIONS
    try {
        $env:JAVA_HOME = ''
        $env:PATH = "$env:SystemRoot\System32;$env:SystemRoot"
        $env:JAVA_TOOL_OPTIONS = '-Dbandlogger.dataDir="' + (Join-Path $extracted 'test data') + '"'
        $p = Start-Process -FilePath (Join-Path $extracted 'band.logger\band.logger.exe') -ArgumentList '--smoke-test' -PassThru -WindowStyle Hidden
        if (-not $p.WaitForExit(60000)) { $p.Kill(); throw 'Extracted package timed out.' }
        if ($p.ExitCode -ne 0 -or -not (Test-Path (Join-Path $extracted 'test data\smoke-passed.txt'))) { throw 'Extracted ZIP smoke test failed.' }
        $restart = Start-Process -FilePath (Join-Path $extracted 'band.logger\band.logger.exe') -ArgumentList '--event-restart-check' -PassThru -WindowStyle Hidden
        if (-not $restart.WaitForExit(60000)) { $restart.Kill(); throw 'Extracted event restart timed out.' }
        if ($restart.ExitCode -ne 0 -or -not (Test-Path (Join-Path $extracted 'test data\event-restart-passed.txt'))) { throw 'Extracted event restart smoke failed.' }
    } finally {
        $env:JAVA_HOME = $previousJava
        $env:PATH = $previousPath
        $env:JAVA_TOOL_OPTIONS = $previousOptions
    }
    Get-FileHash "dist\band.logger-$version-windows-x64.zip" -Algorithm SHA256 |
        Format-List | Out-File "dist\SHA256-$version.txt"
} finally { Pop-Location }
