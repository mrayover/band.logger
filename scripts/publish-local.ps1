param(
    [Parameter(Mandatory)][string]$Archive,
    [string]$OutputRoot = (Join-Path (Split-Path $PSScriptRoot) 'dist'),
    [switch]$VerifyExisting
)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot
$archivePath = (Resolve-Path -LiteralPath $Archive).Path
$name = [IO.Path]::GetFileName($archivePath)
if ($name -notmatch '^band\.logger-([0-9]+\.[0-9]+\.[0-9]+)-windows-x64\.zip$') { throw 'Use a versioned band.logger Windows ZIP.' }
$version = $Matches[1]
New-Item -ItemType Directory -Force -Path $OutputRoot | Out-Null
$output = (Resolve-Path -LiteralPath $OutputRoot).Path
if ((Get-Item -LiteralPath $output).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Delivery root must be a normal directory.' }
$target = Join-Path $output "band.logger-$version-windows-x64"
$existing = Test-Path -LiteralPath $target
if ($existing -and -not $VerifyExisting) { throw "Extracted version already exists; it is preserved: $target" }
if ($existing -and ((Get-Item -LiteralPath $target).Attributes -band [IO.FileAttributes]::ReparsePoint)) { throw 'Existing delivery must be a normal directory.' }
$stage = Join-Path $output ('.extract-' + $version + '-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $stage | Out-Null
$stageImage = Join-Path $stage 'band.logger'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::OpenRead($archivePath)
try {
    $files = @($zip.Entries | Where-Object { -not $_.FullName.EndsWith('/') })
    if ($files.Count -eq 0) { throw 'Empty release archive.' }
    $seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach ($entry in $zip.Entries) {
        $relative = $entry.FullName.Replace('\','/')
        if (-not $relative.StartsWith('band.logger/') -or $relative.Split('/') -contains '..' -or $relative.Split('/') -contains '.' -or $relative.Contains(':')) { throw "Unsafe archive path: $relative" }
        if (-not $seen.Add($relative)) { throw "Duplicate archive entry: $relative" }
    }
    Expand-Archive -LiteralPath $archivePath -DestinationPath $stage
    foreach ($entry in $files) {
        $path = Join-Path $stage $entry.FullName
        $inputStream = $entry.Open()
        $sha = [Security.Cryptography.SHA256]::Create()
        try { $expected = [Convert]::ToHexString($sha.ComputeHash($inputStream)) }
        finally { $inputStream.Dispose(); $sha.Dispose() }
        if (-not (Test-Path -LiteralPath $path -PathType Leaf) -or (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ne $expected) { throw "Extracted file verification failed: $path" }
    }
    if (@(Get-ChildItem -LiteralPath $stageImage -File -Force -Recurse).Count -ne $files.Count) { throw 'Unexpected extracted files.' }
} finally { $zip.Dispose() }
foreach ($required in @('band.logger.exe','app/band.logger.cfg','runtime/bin/server/jvm.dll','runtime/release')) {
    if (-not (Test-Path -LiteralPath (Join-Path $stageImage $required) -PathType Leaf)) { throw "Incomplete Windows image: $required" }
}
if (@(Get-ChildItem (Join-Path $stageImage 'app') -Filter '*.jar').Count -eq 0) { throw 'Application JARs missing.' }
if ($existing) {
    # Recovery after a late shortcut failure: compare every byte; never replace program files.
    if (@(Get-ChildItem -LiteralPath $target -File -Force -Recurse).Count -ne $files.Count) { throw 'Existing version inventory differs from its ZIP.' }
    foreach ($file in Get-ChildItem -LiteralPath $stageImage -File -Force -Recurse) {
        $relative = [IO.Path]::GetRelativePath($stageImage,$file.FullName)
        $installed = Join-Path $target $relative
        if (-not (Test-Path -LiteralPath $installed -PathType Leaf) -or (Get-FileHash -LiteralPath $installed).Hash -ne (Get-FileHash -LiteralPath $file.FullName).Hash) { throw "Existing version differs from ZIP: $relative" }
    }
}
$smokeImage = if ($existing) { $target } else { $stageImage }
# Diagnostics write only to a new synthetic directory, never beside the executable or to production.
$smoke = Join-Path $root ('.tools/local-delivery-smoke-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $smoke | Out-Null
$previousJava = $env:JAVA_HOME; $previousPath = $env:PATH; $previousOptions = $env:JAVA_TOOL_OPTIONS
try {
    $env:JAVA_HOME = ''
    $env:PATH = "$env:SystemRoot\System32;$env:SystemRoot"
    $env:JAVA_TOOL_OPTIONS = '-Dbandlogger.dataDir="' + $smoke + '"'
    foreach ($mode in @('--smoke-test','--event-restart-check')) {
        $p = Start-Process -FilePath (Join-Path $smokeImage 'band.logger.exe') -ArgumentList $mode -PassThru -WindowStyle Hidden
        if (-not $p.WaitForExit(60000)) { $p.Kill(); throw "Local delivery $mode timed out." }
        if ($p.ExitCode -ne 0) { throw "Local delivery $mode failed with exit $($p.ExitCode)." }
    }
    foreach ($marker in @('smoke-passed.txt','event-restart-passed.txt','event-history-passed.txt','event-history-restart-passed.txt','numbered-routing-passed.txt','numbered-routing-restart-passed.txt')) {
        if (-not (Test-Path -LiteralPath (Join-Path $smoke $marker))) { throw "Missing executable verification: $marker" }
    }
} finally { $env:JAVA_HOME = $previousJava; $env:PATH = $previousPath; $env:JAVA_TOOL_OPTIONS = $previousOptions }
# A fresh version is published without replacement. Verify resolved move paths stay in this delivery root.
$prefix = $output.TrimEnd('\','/') + [IO.Path]::DirectorySeparatorChar
foreach ($path in @($stageImage,$target)) {
    if (-not [IO.Path]::GetFullPath($path).StartsWith($prefix,[StringComparison]::OrdinalIgnoreCase)) { throw 'Delivery path escaped its root.' }
}
if (-not $existing) {
    if (Test-Path -LiteralPath $target) { throw 'Another delivery already published this version; no files overwritten.' }
    Move-Item -LiteralPath $stageImage -Destination $target
    Remove-Item -LiteralPath $stage # empty only, never recursive
}
$exe = Join-Path $target 'band.logger.exe'
# Only the shortcut changes; all old extracted versions and any running process are untouched.
$current = Join-Path $output 'Launch current build.lnk'
$tempLink = Join-Path $output ('current-' + [guid]::NewGuid().ToString('N') + '.lnk')
$shell = New-Object -ComObject WScript.Shell
$link = $shell.CreateShortcut($tempLink)
$link.TargetPath = $exe
$link.WorkingDirectory = $target
$link.Description = "Verified band.logger $version"
$link.Save()
if ($shell.CreateShortcut($tempLink).TargetPath -ne $exe) { throw 'Current-build shortcut verification failed.' }
if (Test-Path -LiteralPath $current) { [IO.File]::Replace($tempLink,$current,[NullString]::Value) }
else { [IO.File]::Move($tempLink,$current) }
[pscustomobject]@{ Version=$version; Executable=$exe; Archive=$archivePath; VerifiedFiles=$files.Count; SmokeData=$smoke } |
    ConvertTo-Json | Set-Content -LiteralPath (Join-Path $root ".tools/local-delivery-$version.json")
Write-Output "Ready to launch: $exe"
