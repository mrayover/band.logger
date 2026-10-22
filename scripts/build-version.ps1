function Get-BandLoggerVersion {
    param([string]$Root)
    $text = [IO.File]::ReadAllText((Join-Path $Root 'app/build.gradle.kts'))
    $match = [regex]::Match($text, 'packageVersion\s*=\s*"([0-9]+\.[0-9]+\.[0-9]+)"')
    if (-not $match.Success) { throw 'Cannot read application packageVersion.' }
    $match.Groups[1].Value
}
