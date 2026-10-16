param([string]$RepoRoot = (Split-Path -Parent $PSScriptRoot))

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = [IO.Path]::GetFullPath($RepoRoot)
$queuePath = Join-Path $root 'docs/remaster/QUEUE.md'
$promptDirectory = Join-Path $root 'docs/remaster/prompts'
$tasks = @{}
$pattern = '^\| ([PR]\d{2}) \| \[[^\]]+\]\(([^)]+)\) \| ([^|]+) \| ([^|]+) \|$'
foreach ($line in (Get-Content -LiteralPath $queuePath -Encoding UTF8)) {
    if ($line -notmatch $pattern) { continue }
    $id = $Matches[1]
    if ($tasks.ContainsKey($id)) { throw "Duplicate task: $id" }
    $path = [IO.Path]::GetFullPath((Join-Path (Split-Path $queuePath) $Matches[2]))
    $model = $Matches[3].Trim()
    $deps = @([regex]::Matches($Matches[4], '[PR]\d{2}') | ForEach-Object { $_.Value })
    $order = $tasks.Count + 1
    $name = [IO.Path]::GetFileName($path)
    if ([IO.Path]::GetDirectoryName($path) -ne $promptDirectory -or
        $name -notmatch ('^{0:d2}-{1}-.+\.md$' -f $order, $id)) {
        throw "Prompt outside canonical folder or out of sequence: $id"
    }
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing prompt: $path" }
    $body = Get-Content -LiteralPath $path -Raw -Encoding UTF8
    $header = [regex]::Match($body, '(GPT-[^;\r\n]+); Reasoning: (\w+)')
    if (-not $header.Success -or ($header.Groups[1].Value + ' / ' + $header.Groups[2].Value) -ne $model) {
        throw "Prompt/queue model mismatch: $id"
    }
    if ($body -notmatch '(?m)^- \[[ xX]\] ') { throw "No progress checklist: $id" }
    $declared = [regex]::Match($body, '(?m)^\*\*[^:\r\n]+:\s*((?:[PR]\d{2}(?:,\s*)?)+)\.\*\*')
    $promptDeps = @([regex]::Matches($declared.Groups[1].Value, '[PR]\d{2}') | ForEach-Object { $_.Value })
    if (@($promptDeps | Where-Object { $_ -notin $deps }).Count -gt 0 -or
        @($deps | Where-Object { $_ -notin $promptDeps }).Count -gt 0) {
        throw "Prompt/queue dependency mismatch: $id"
    }
    $tasks[$id] = @{ Path = $path; Dependencies = $deps; Body = $body; Order = $order }
}
foreach ($id in @((1..28 | ForEach-Object { 'P{0:d2}' -f $_ }) + (1..27 | ForEach-Object { 'R{0:d2}' -f $_ }))) {
    if (-not $tasks.ContainsKey($id)) { throw "Missing queue entry: $id" }
}
foreach ($file in (Get-ChildItem -LiteralPath $promptDirectory -Filter '*.md' -File)) {
    if ($file.Name -eq '00-START-HERE.md') { continue }
    if ($file.FullName -notin @($tasks.Values | ForEach-Object { $_.Path })) {
        throw "Unregistered prompt file: $($file.Name)"
    }
}
if (@(Get-ChildItem -LiteralPath (Join-Path $root 'docs/implementation/prompts') -Filter '*.md' -File |
    Where-Object { $_.Name -ne 'README.md' }).Count -gt 0) { throw 'Duplicate legacy prompt folder: move full prompts to the canonical folder' }

$state = @{}
function Visit-Task([string]$Id) {
    if (-not $tasks.ContainsKey($Id)) { throw "Unknown dependency: $Id" }
    if ($state.ContainsKey($Id)) {
        if ($state[$Id] -eq 'visiting') { throw "Dependency cycle at $Id" }
        return
    }
    $state[$Id] = 'visiting'
    foreach ($dep in $tasks[$Id].Dependencies) { Visit-Task $dep }
    $state[$Id] = 'done'
}
foreach ($id in $tasks.Keys) { Visit-Task $id }
# Conditional paid/sharing release gates must also remain acyclic.
$baseReleaseDeps = $tasks['P28'].Dependencies
$tasks['P28'].Dependencies += @('R24', 'R26')
$state.Clear()
foreach ($id in $tasks.Keys) { Visit-Task $id }
foreach ($id in $tasks.Keys) {
    foreach ($dep in $tasks[$id].Dependencies) {
        if ($tasks[$dep].Order -ge $tasks[$id].Order) { throw "Prerequisite must appear earlier in sequence: $id <- $dep" }
    }
}
$tasks['P28'].Dependencies = $baseReleaseDeps

$statuses = @{}
$verified = @('done', 'not_reproduced_verified')
$commonStatuses = @('pending', 'in_progress', 'implemented_needs_device', 'needs_owner', 'needs_beta_data', 'blocked_dependency', 'done')
foreach ($progress in @('docs/implementation/PROGRESS.md', 'docs/remaster/PROGRESS.md')) {
    $prefix = if ($progress -like 'docs/implementation/*') { 'P' } else { 'R' }
    $allowed = $commonStatuses + $(if ($prefix -eq 'P') { 'not_reproduced_verified' } else { 'deferred' })
    foreach ($line in (Get-Content -LiteralPath (Join-Path $root $progress) -Encoding UTF8)) {
        if ($line -notmatch '^\| [PR]\d{2}\b') { continue }
        if ($line -notmatch '^\| ([PR]\d{2}) \| [^|]+ \| ([^|]+) \|') { throw "Malformed progress row: $line" }
        $id = $Matches[1]
        $status = $Matches[2].Trim()
        if (-not $tasks.ContainsKey($id)) { throw "Progress has unknown task: $id" }
        if (-not $id.StartsWith($prefix)) { throw "Task in wrong progress registry: $id" }
        if ($statuses.ContainsKey($id)) { throw "Duplicate progress entry: $id" }
        if ($status -notin $allowed) { throw "Invalid progress status: $id = $status" }
        $statuses[$id] = $status
        $body = $tasks[$id].Body
        $hasChecked = $body -match '(?m)^- \[[xX]\] '
        $hasUnchecked = $body -match '(?m)^- \[ \] '
        if ($status -in $verified -and $hasUnchecked) { throw "Verified task has unchecked gates: $id" }
        if ($status -eq 'pending' -and $hasChecked) { throw "Pending task has checked gates: $id" }
        if ($status -notin $verified -and -not $hasUnchecked) { throw "Unverified task has all gates checked: $id" }
        if ($hasChecked -or $status -in ($verified + @('implemented_needs_device'))) {
            $relativeEvidence = "evidence/$id.md"
            $evidencePath = Join-Path (Split-Path (Join-Path $root $progress)) $relativeEvidence
            if (-not (Test-Path -LiteralPath $evidencePath -PathType Leaf) -or
                [string]::IsNullOrWhiteSpace([IO.File]::ReadAllText($evidencePath))) {
                throw "Missing or empty evidence: $id"
            }
            if ($line -notmatch ('\]\(' + [regex]::Escape($relativeEvidence) + '(?:#[^)]*)?\)')) {
                throw "Progress must link its evidence: $id"
            }
        }
    }
}
foreach ($id in $tasks.Keys) {
    if (-not $statuses.ContainsKey($id)) { throw "Missing progress entry: $id" }
}

$queueBody = Get-Content -LiteralPath $queuePath -Raw -Encoding UTF8
function Read-CandidateField([string]$Name, [string[]]$AllowedValues) {
    $matches = [regex]::Matches($queueBody, '(?m)^' + [regex]::Escape($Name) + ': ([^\r\n]+)\r?$')
    if ($matches.Count -ne 1 -or $matches[0].Groups[1].Value -notin $AllowedValues) {
        throw "Missing, duplicate or invalid candidate field: $Name"
    }
    return $matches[0].Groups[1].Value
}
$releaseScope = Read-CandidateField 'Release candidate scope' @('undecided', 'free', 'paid')
$sharing = Read-CandidateField 'Sharing in candidate' @('undecided', 'no', 'yes')
$activeTask = Read-CandidateField 'Active task' (@('none') + @($tasks.Keys))
$nextTask = Read-CandidateField 'Next task without device' (@('none') + @($tasks.Keys))
$deviceGate = Read-CandidateField 'Next device gate' (@('none') + @($tasks.Keys))
foreach ($pointer in @($activeTask, $nextTask, $deviceGate)) {
    if ($pointer -ne 'none' -and $statuses[$pointer] -in ($verified + @('deferred'))) {
        throw "Current-stage pointer targets completed or deferred task: $pointer"
    }
}
if ($statuses['P28'] -in $verified) {
    if ($releaseScope -eq 'undecided' -or $sharing -eq 'undecided') { throw 'Completed P28 requires explicit candidate scope and sharing decision' }
    if ($releaseScope -eq 'paid') { $tasks['P28'].Dependencies += 'R24' }
    if ($sharing -eq 'yes') { $tasks['P28'].Dependencies += 'R26' }
}
foreach ($id in $tasks.Keys) {
    if ($statuses[$id] -notin $verified) { continue }
    foreach ($dep in $tasks[$id].Dependencies) {
        if ($statuses[$dep] -notin $verified) { throw "Verified task $id has unfinished prerequisite: $dep ($($statuses[$dep]))" }
    }
}

$scopes = @('docs/remaster', 'docs/design', 'docs/agent-workflow', 'docs/implementation/prompts')
$files = @($scopes | ForEach-Object { Get-ChildItem -LiteralPath (Join-Path $root $_) -Recurse -Filter '*.md' -File })
$files += @('REMASTER_PLAN.md', 'MONETIZATION_STRATEGY.md', 'SUBSCRIPTION_RULES.md', 'IMPLEMENTATION_PROMPTS.md',
    'README.md', 'PRODUCT_STRATEGY.md', 'MVP_DEFINITION.md', 'docs/PROMPTS_TO_10_10.md', 'docs/implementation/PROGRESS.md') |
    ForEach-Object { Get-Item -LiteralPath (Join-Path $root $_) }
$links = 0
foreach ($file in $files) {
    $body = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
    foreach ($match in [regex]::Matches($body, '\]\(([^)]+)\)')) {
        $link = $match.Groups[1].Value
        if ($link -match '^(https?://|mailto:|#)') { continue }
        $relative = $link.Split('#')[0]
        if (-not (Test-Path -LiteralPath (Join-Path $file.DirectoryName $relative))) {
            throw "Broken local link in $($file.FullName): $link"
        }
        $links++
    }
}
foreach ($task in $tasks.Values) {
    foreach ($match in [regex]::Matches($task.Body, '`((?:app|domain|gradle)/[^`\r\n]+)`')) {
        $entry = $match.Groups[1].Value
        if ($entry -match '[*<>]' -or $entry -notmatch '\.(kt|kts)$') { continue }
        if (-not (Test-Path -LiteralPath (Join-Path $root $entry))) { throw "Missing source entry: $entry" }
    }
}
$graphLines = @(Get-Content -LiteralPath (Join-Path $root 'GRAPH_REPORT.md') -Encoding UTF8).Count
if ($graphLines -gt 200) { Write-Warning "GRAPH_REPORT has $graphLines lines; keep it compact." }
Write-Output "PASS: $($tasks.Count) prompts in one ordered folder, models/checklists, evidence/status/prerequisites, current pointers, dependency DAG including conditional release gates, $links local links."
Write-Output 'Evidence presence and consistency are checked; test results, human approvals and artifact scope still require review.'
