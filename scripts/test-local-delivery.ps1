$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot
$test = Join-Path $root ('.tools/delivery-safety-' + [guid]::NewGuid().ToString('N'))
$output = Join-Path $test 'releases'
$fixture = Join-Path $test 'fixture/band.logger'
New-Item -ItemType Directory -Force -Path $output,$fixture | Out-Null
$current = Join-Path $output 'Launch current build.lnk'
[IO.File]::WriteAllText($current,'last working shortcut sentinel')
$prior = Join-Path $output 'band.logger-0.9.9-windows-x64'
New-Item -ItemType Directory -Path $prior | Out-Null
[IO.File]::WriteAllText((Join-Path $prior 'keep.txt'),'previous working version')
$archive = Join-Path $test 'band.logger-0.10.0-windows-x64.zip'
[IO.File]::WriteAllText((Join-Path $fixture 'band.logger.exe'),'synthetic incomplete image, never launched')
Compress-Archive -LiteralPath $fixture -DestinationPath $archive
function Assert-Preserved {
    if ([IO.File]::ReadAllText($current) -ne 'last working shortcut sentinel' -or [IO.File]::ReadAllText((Join-Path $prior 'keep.txt')) -ne 'previous working version') { throw 'Last working delivery changed after failure.' }
}
$rejected = $false
try { & "$PSScriptRoot/publish-local.ps1" -Archive $archive -OutputRoot $output }
catch { if ($_.Exception.Message -notlike 'Incomplete Windows image:*') { throw }; $rejected = $true }
if (-not $rejected) { throw 'Incomplete archive was accepted.' }
Assert-Preserved
$existing = Join-Path $output 'band.logger-0.10.0-windows-x64'
New-Item -ItemType Directory -Path $existing | Out-Null
[IO.File]::WriteAllText((Join-Path $existing 'running-version.txt'),'must never overwrite')
$rejected = $false
try { & "$PSScriptRoot/publish-local.ps1" -Archive $archive -OutputRoot $output }
catch { if ($_.Exception.Message -notlike 'Extracted version already exists*') { throw }; $rejected = $true }
if (-not $rejected -or [IO.File]::ReadAllText((Join-Path $existing 'running-version.txt')) -ne 'must never overwrite') { throw 'Existing version protection failed.' }
Assert-Preserved
$rejected = $false
try { & "$PSScriptRoot/publish-local.ps1" -Archive $archive -OutputRoot $output -VerifyExisting }
catch { if ($_.Exception.Message -notlike 'Incomplete Windows image:*') { throw }; $rejected = $true }
if (-not $rejected) { throw 'Recovery accepted an incomplete image.' }
Assert-Preserved
# PowerShell needs NullString.Value to preserve a null backup path for File.Replace.
$tempPointer = Join-Path $test 'pointer-new.txt'
$oldPointer = Join-Path $test 'pointer-current.txt'
[IO.File]::WriteAllText($tempPointer,'new verified target')
[IO.File]::WriteAllText($oldPointer,'old target')
[IO.File]::Replace($tempPointer,$oldPointer,[NullString]::Value)
if ([IO.File]::ReadAllText($oldPointer) -ne 'new verified target') { throw 'Atomic current pointer update failed.' }
# Invalid traversal is rejected before extraction, with the current pointer unchanged.
$unsafe = Join-Path $test 'band.logger-0.10.1-windows-x64.zip'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::Open($unsafe,[IO.Compression.ZipArchiveMode]::Create)
try { $entry=$zip.CreateEntry('band.logger/../../escaped.txt'); $writer=[IO.StreamWriter]::new($entry.Open()); try {$writer.Write('must not escape')} finally {$writer.Dispose()} } finally {$zip.Dispose()}
$rejected = $false
try { & "$PSScriptRoot/publish-local.ps1" -Archive $unsafe -OutputRoot $output }
catch { if ($_.Exception.Message -notlike 'Unsafe archive path:*') { throw }; $rejected = $true }
if (-not $rejected -or (Test-Path -LiteralPath (Join-Path $output 'escaped.txt'))) { throw 'Unsafe archive accepted.' }
Assert-Preserved
'PASS: incomplete archives, existing versions and traversal preserve prior builds/current shortcut.'
