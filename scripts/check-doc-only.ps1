$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$changedKotlin = git status --porcelain -- "*.kt" "*.kts"

if ($changedKotlin) {
    Write-Error "Kotlin/KTS files changed:`n$changedKotlin"
}

Write-Host "OK: no Kotlin/KTS changes detected."
