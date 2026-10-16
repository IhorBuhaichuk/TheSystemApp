param([string]$RepoRoot = (Split-Path -Parent $PSScriptRoot))

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$checker = Join-Path ([IO.Path]::GetFullPath($RepoRoot)) 'scripts/check-remaster-plan.ps1'
$engine = (Get-Process -Id $PID).Path
$tempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
$fixtureBase = Join-Path $tempRoot ('the-system-plan-tests-' + [guid]::NewGuid().ToString('N'))
$ids = @('P01', 'P02', 'R01', 'R02', 'R03', 'R04', 'R05', 'R06', 'P03', 'P04', 'P05', 'P06') +
    @(7..9 | ForEach-Object { 'P{0:d2}' -f $_ }) + @('R27') +
    @(10..17 | ForEach-Object { 'P{0:d2}' -f $_ }) +
    @('R07', 'R08', 'R09', 'R10', 'R11', 'R12', 'R13', 'R14', 'R16', 'P18', 'P19', 'R15',
        'P20', 'P21', 'P22', 'P23', 'P24', 'P25', 'P26', 'R25', 'P27', 'R17', 'R18', 'R19',
        'R20', 'R21', 'R22', 'R23', 'R24', 'R26', 'P28')
$script:caseCount = 0

function Write-FixtureFile([string]$Path, [string]$Text) {
    $destination = Join-Path $script:fixtureRoot $Path
    [void][IO.Directory]::CreateDirectory((Split-Path $destination))
    [IO.File]::WriteAllText($destination, $Text, [Text.UTF8Encoding]::new($false))
}

function New-Fixture {
    $script:caseCount++
    $script:fixtureRoot = Join-Path $fixtureBase ('case-{0:d2}' -f $script:caseCount)
    $script:statuses = @{}
    $script:deps = @{}
    $script:scope = 'undecided'
    $script:sharing = 'undecided'
    foreach ($id in $ids) {
        $script:statuses[$id] = 'pending'
        $script:deps[$id] = @()
    }
    $script:deps['P02'] = @('P01')
    $script:deps['P28'] = @('P27', 'R25')
    foreach ($directory in @('docs/remaster/prompts', 'docs/implementation/prompts', 'docs/design', 'docs/agent-workflow')) {
        [void][IO.Directory]::CreateDirectory((Join-Path $script:fixtureRoot $directory))
    }
    foreach ($file in @('REMASTER_PLAN.md', 'MONETIZATION_STRATEGY.md', 'SUBSCRIPTION_RULES.md',
        'IMPLEMENTATION_PROMPTS.md', 'README.md', 'PRODUCT_STRATEGY.md', 'MVP_DEFINITION.md',
        'docs/PROMPTS_TO_10_10.md', 'GRAPH_REPORT.md')) {
        Write-FixtureFile $file '# Synthetic fixture'
    }
}

function Task-Path([string]$Id) {
    return ('docs/remaster/prompts/{0:d2}-{1}-fixture.md' -f ([Array]::IndexOf($ids, $Id) + 1), $Id)
}

function Write-Fixture {
    $queue = @('# Synthetic queue', "Release candidate scope: $script:scope", "Sharing in candidate: $script:sharing",
        'Active task: none', 'Next task without device: R01', 'Next device gate: P03')
    $pProgress = @('# P progress')
    $rProgress = @('# R progress')
    foreach ($id in $ids) {
        $taskPath = Task-Path $id
        $depText = if ($script:deps[$id].Count -eq 0) { '-' } else { $script:deps[$id] -join ', ' }
        $relative = $taskPath.Substring('docs/remaster/'.Length)
        $queue += "| $id | [$id]($relative) | GPT-6.1 Sol / High | $depText |"
        $status = $script:statuses[$id]
        $checked = if ($status -in @('done', 'not_reproduced_verified', 'implemented_needs_device')) { 'x' } else { ' ' }
        $second = if ($status -in @('done', 'not_reproduced_verified')) { 'x' } else { ' ' }
        Write-FixtureFile $taskPath "# $id`n**Model: GPT-6.1 Sol; Reasoning: High.**`n**Dependencies: $depText.**`n- [$checked] Implemented.`n- [$second] Verified.`n"
        $evidence = 'No evidence yet'
        if ($checked -eq 'x') {
            $area = if ($id.StartsWith('P')) { 'implementation' } else { 'remaster' }
            Write-FixtureFile "docs/$area/evidence/$id.md" "# $id`nSynthetic test evidence; not an actual app result."
            $evidence = "[Evidence](evidence/$id.md)"
        }
        $row = "| $id | Synthetic task | $status | $evidence |"
        if ($id.StartsWith('P')) { $pProgress += $row } else { $rProgress += $row }
    }
    Write-FixtureFile 'docs/remaster/QUEUE.md' ($queue -join "`n")
    Write-FixtureFile 'docs/implementation/PROGRESS.md' ($pProgress -join "`n")
    Write-FixtureFile 'docs/remaster/PROGRESS.md' ($rProgress -join "`n")
}

function Replace-FixtureText([string]$Path, [string]$Before, [string]$After) {
    $body = [IO.File]::ReadAllText((Join-Path $script:fixtureRoot $Path))
    if (-not $body.Contains($Before)) { throw "Fixture replacement not found: $Before" }
    Write-FixtureFile $Path ($body.Replace($Before, $After))
}

function Set-FreeRelease {
    $script:scope = 'free'
    $script:sharing = 'no'
    foreach ($id in @('P27', 'P28', 'R25')) { $script:statuses[$id] = 'done' }
    foreach ($id in @((17..24 | ForEach-Object { 'R{0:d2}' -f $_ }) + 'R26')) { $script:statuses[$id] = 'deferred' }
}

function Test-Case([string]$Name, [scriptblock]$Setup, [scriptblock]$Mutate, [string]$ExpectedError = '') {
    New-Fixture
    & $Setup
    Write-Fixture
    & $Mutate
    # Isolate the guard process; a deliberate native failure must not abort the harness.
    $savedPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $output = (& $engine -NoProfile -ExecutionPolicy Bypass -File $checker -RepoRoot $script:fixtureRoot 2>&1 | Out-String)
        $code = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $savedPreference
    }
    if ($ExpectedError -eq '') {
        if ($code -ne 0 -or $output -notmatch 'PASS: 55 prompts') { throw "Expected PASS: $Name`n$output" }
    } elseif ($code -eq 0 -or -not $output.Contains($ExpectedError)) {
        throw "Expected rejection '$ExpectedError': $Name (exit $code)`n$output"
    }
    Write-Output "PASS: $Name"
}

try {
    Test-Case 'All pending is valid' {} {}
    Test-Case 'Verified chain and partial device gate' {
        $script:statuses['P01'] = 'done'; $script:statuses['P02'] = 'done'
        $script:statuses['P03'] = 'implemented_needs_device'
    } {}
    Test-Case 'Not reproduced requires verified evidence' { $script:statuses['P01'] = 'not_reproduced_verified' } {}
    Test-Case 'Missing evidence is rejected' { $script:statuses['P01'] = 'done' } {
        [IO.File]::Delete((Join-Path $script:fixtureRoot 'docs/implementation/evidence/P01.md'))
    } 'Missing or empty evidence: P01'
    Test-Case 'Empty evidence is rejected' { $script:statuses['P01'] = 'done' } {
        Write-FixtureFile 'docs/implementation/evidence/P01.md' '   '
    } 'Missing or empty evidence: P01'
    Test-Case 'Evidence must be linked from progress' { $script:statuses['P01'] = 'done' } {
        Replace-FixtureText 'docs/implementation/PROGRESS.md' '[Evidence](evidence/P01.md)' 'Unlinked evidence'
    } 'Progress must link its evidence: P01'
    Test-Case 'Unfinished prerequisite is rejected' { $script:statuses['P02'] = 'done' } {} 'Verified task P02 has unfinished prerequisite: P01'
    Test-Case 'Done with unchecked gate is rejected' { $script:statuses['P01'] = 'done' } {
        Replace-FixtureText (Task-Path 'P01') '- [x] Verified.' '- [ ] Verified.'
    } 'Verified task has unchecked gates: P01'
    Test-Case 'Pending with uppercase checked gate is rejected' {} {
        Replace-FixtureText (Task-Path 'P01') '- [ ] Implemented.' '- [X] Implemented.'
    } 'Pending task has checked gates: P01'
    Test-Case 'Partial status cannot claim all gates checked' { $script:statuses['P01'] = 'implemented_needs_device' } {
        Replace-FixtureText (Task-Path 'P01') '- [ ] Verified.' '- [x] Verified.'
    } 'Unverified task has all gates checked: P01'
    Test-Case 'Missing progress row is rejected' {} {
        Replace-FixtureText 'docs/implementation/PROGRESS.md' '| P01 | Synthetic task | pending | No evidence yet |' ''
    } 'Missing progress entry: P01'
    Test-Case 'Duplicate progress row is rejected' {} {
        $row = '| P01 | Synthetic task | pending | No evidence yet |'
        Replace-FixtureText 'docs/implementation/PROGRESS.md' $row "$row`n$row"
    } 'Duplicate progress entry: P01'
    Test-Case 'Unknown status is rejected' { $script:statuses['P01'] = 'complete' } {} 'Invalid progress status: P01'
    Test-Case 'Dependency cycle is rejected' { $script:deps['P01'] = @('P02') } {} 'Dependency cycle'
    Test-Case 'Conditional dependency cycle is rejected' { $script:deps['R24'] = @('P28') } {} 'Dependency cycle'
    Test-Case 'Model mismatch is rejected' {} {
        Replace-FixtureText (Task-Path 'P01') 'Reasoning: High' 'Reasoning: Medium'
    } 'Prompt/queue model mismatch: P01'
    Test-Case 'R prompt dependency mismatch is rejected' { $script:deps['R11'] = @('R09') } {
        Replace-FixtureText (Task-Path 'R11') '**Dependencies: R09.**' '**Dependencies: -.**'
    } 'Prompt/queue dependency mismatch: R11'
    Test-Case 'P prompt cannot omit a queue dependency' {} {
        Replace-FixtureText (Task-Path 'P02') '**Dependencies: P01.**' '**Dependencies: -.**'
    } 'Prompt/queue dependency mismatch: P02'
    Test-Case 'Today adjustment must precede rewards' { $script:deps['R27'] = @('P10') } {} 'Prerequisite must appear earlier in sequence: R27 <- P10'
    Test-Case 'Free release permits deferred paid and sharing branches' { Set-FreeRelease } {}
    Test-Case 'Paid release requires R24' { Set-FreeRelease; $script:scope = 'paid' } {} 'Verified task P28 has unfinished prerequisite: R24'
    Test-Case 'Shipped sharing requires R26' { Set-FreeRelease; $script:sharing = 'yes' } {} 'Verified task P28 has unfinished prerequisite: R26'
    Test-Case 'Completed release cannot have undecided scope' { Set-FreeRelease; $script:scope = 'undecided' } {} 'Completed P28 requires explicit candidate scope'
    Test-Case 'Invalid candidate scope is rejected' { $script:scope = 'other' } {} 'Missing, duplicate or invalid candidate field: Release candidate scope'
    Test-Case 'Paid and sharing completion accepts verified prerequisites' {
        Set-FreeRelease; $script:scope = 'paid'; $script:sharing = 'yes'
        $script:statuses['R24'] = 'done'; $script:statuses['R26'] = 'done'
        $script:deps['R24'] = @('P27'); $script:deps['R26'] = @('P27')
    } {}
    Test-Case 'Transitive incomplete paid prerequisite is rejected' {
        Set-FreeRelease; $script:scope = 'paid'; $script:statuses['R24'] = 'done'
        $script:deps['R24'] = @('R23')
    } {} 'Verified task R24 has unfinished prerequisite: R23'
    Test-Case 'Dependency must precede its consumer' { $script:deps['P01'] = @('R01') } {} 'Prerequisite must appear earlier in sequence: P01 <- R01'
    Test-Case 'Extra prompt cannot be hidden outside queue' {} {
        Write-FixtureFile 'docs/remaster/prompts/55-R99-unregistered.md' '# Unregistered fixture'
    } 'Unregistered prompt file'
    Test-Case 'Legacy full prompt copy is rejected' {} {
        Write-FixtureFile 'docs/implementation/prompts/01-old.md' '# Duplicate fixture'
    } 'Duplicate legacy prompt folder'
    Test-Case 'Filename order must match queue order' {} {
        $old = Task-Path 'P01'
        $new = 'docs/remaster/prompts/99-P01-fixture.md'
        Write-FixtureFile $new ([IO.File]::ReadAllText((Join-Path $script:fixtureRoot $old)))
        Replace-FixtureText 'docs/remaster/QUEUE.md' 'prompts/01-P01-fixture.md' 'prompts/99-P01-fixture.md'
    } 'Prompt outside canonical folder or out of sequence: P01'
    Test-Case 'Next pointer cannot target a completed task' { $script:statuses['P01'] = 'done' } {
        Replace-FixtureText 'docs/remaster/QUEUE.md' 'Next task without device: R01' 'Next task without device: P01'
    } 'Current-stage pointer targets completed or deferred task: P01'
    Write-Output "PASS: $script:caseCount plan-guard scenarios; no repository fixtures modified."
} finally {
    # Delete only the unique, resolved test directory under the OS temp directory.
    $resolved = [IO.Path]::GetFullPath($fixtureBase)
    $boundary = $tempRoot.TrimEnd([IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    if (-not $resolved.StartsWith($boundary, [StringComparison]::OrdinalIgnoreCase) -or
        [IO.Path]::GetFileName($resolved) -notmatch '^the-system-plan-tests-[0-9a-f]{32}$') {
        throw "Unsafe fixture cleanup path: $resolved"
    }
    if (Test-Path -LiteralPath $resolved) { Remove-Item -LiteralPath $resolved -Recurse -Force }
}
