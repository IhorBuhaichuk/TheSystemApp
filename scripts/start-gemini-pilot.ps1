param(
    [ValidateSet('G01', 'G02', 'G03')]
    [string]$Task,
    [string]$Model,
    [ValidateSet('low', 'medium', 'high')]
    [string]$Effort
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$binary = Join-Path $env:LOCALAPPDATA 'agy\bin\agy.exe'
if (-not (Test-Path -LiteralPath $binary -PathType Leaf)) {
    throw 'Antigravity CLI not installed in the expected user-local directory.'
}

Push-Location $root
try {
    if (-not $Task) {
        & $binary --mode plan
        exit $LASTEXITCODE
    }
    if ([string]::IsNullOrWhiteSpace($Model)) {
        throw 'Specify -Model using an exact ID from agy models. No silent fallback.'
    }

    $progress = Get-Content -LiteralPath 'docs/agent-workflow/gemini-pilot/PROGRESS.md' -Raw
    if ($progress -notmatch "(?m)^\| $Task \|[^\r\n]*\| (claimed|in_progress) \| Gemini \|") {
        throw "$Task must first be claimed for Gemini by the coordinator in pilot PROGRESS.md."
    }
    $previous = @{ G02 = 'G01'; G03 = 'G02' }
    if ($previous.ContainsKey($Task)) {
        $prerequisite = $previous[$Task]
        if ($progress -notmatch "(?m)^\| $prerequisite \|[^\r\n]*\| accepted \|") {
            throw "$prerequisite must be accepted before $Task."
        }
    }

    $models = & $binary models 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Cannot verify model access. Sign in first. $models" }
    $modelText = $models -join "`n"
    if ($modelText -notmatch ('(?m)^\s*' + [regex]::Escape($Model) + '(\s|$)')) {
        throw "Requested model not listed: $Model. Run agy models; choose explicitly."
    }
    if (-not $Effort) { $Effort = if ($Task -eq 'G01') { 'low' } else { 'medium' } }
    $briefs = @{
        G01 = '01-G01-context-handoff.md'
        G02 = '02-G02-parser-review.md'
        G03 = '03-G03-parser-tests.md'
    }
    $brief = 'docs/agent-workflow/gemini-pilot/' + $briefs[$Task]
    $prompt = "Execute only $Task from $brief. Follow GEMINI.md and AGENTS.md. Do not take other tasks."

    if ($Task -eq 'G03') {
        & $binary --mode accept-edits --model $Model --effort $Effort -i $prompt
        exit $LASTEXITCODE
    }

    $reports = Join-Path $root 'docs/agent-workflow/gemini-pilot/reports'
    New-Item -ItemType Directory -Path $reports -Force | Out-Null
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $report = Join-Path $reports "$Task-$stamp.json"
    $diagnostics = Join-Path $reports "$Task-$stamp.stderr.log"
    $response = & $binary --mode plan --model $Model --effort $Effort --output-format json --print-timeout 3m -p $prompt 2> $diagnostics
    $code = $LASTEXITCODE
    $response | Set-Content -LiteralPath $report -Encoding UTF8
    Write-Host "Response: $report"
    Write-Host "Diagnostics: $diagnostics"
    if ($code -ne 0) { throw "Antigravity failed (exit $code). Inspect local diagnostics." }
    $result = ($response -join "`n") | ConvertFrom-Json
    if ($result.status -ne 'SUCCESS') { throw "CLI status: $($result.status); review required." }
    Write-Output $result.response
    Write-Host 'Review-ready only. Coordinator must inspect evidence; exit 0 is not acceptance.'
} finally {
    Pop-Location
}
