param(
    [string]$RepoRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$root = [System.IO.Path]::GetFullPath($RepoRoot).TrimEnd([char[]]"\/")
$prefix = $root + [System.IO.Path]::DirectorySeparatorChar

function Get-CheckedPath([string]$RelativePath) {
    if ([string]::IsNullOrWhiteSpace($RelativePath) -or
        [System.IO.Path]::IsPathRooted($RelativePath) -or
        $RelativePath -match '(^|[\\/])\.\.([\\/]|$)') {
        throw "Expected a repository-relative path: $RelativePath"
    }
    $resolved = [System.IO.Path]::GetFullPath((Join-Path $root $RelativePath))
    if (-not $resolved.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Path leaves repository: $RelativePath"
    }
    return $resolved
}

function Get-Sha256([string]$Path) {
    $algorithm = [System.Security.Cryptography.SHA256]::Create()
    $stream = [System.IO.File]::OpenRead($Path)
    try {
        return [System.BitConverter]::ToString($algorithm.ComputeHash($stream)).Replace('-', '').ToLowerInvariant()
    } finally {
        $stream.Dispose()
        $algorithm.Dispose()
    }
}

$lockPath = Get-CheckedPath 'docs/agent-workflow/skills-lock.json'
$lock = Get-Content -LiteralPath $lockPath -Raw -Encoding UTF8 | ConvertFrom-Json
if ($lock.schemaVersion -ne 1) { throw "Unsupported skill lock schema" }

foreach ($path in $lock.requiredFiles) {
    if (-not (Test-Path -LiteralPath (Get-CheckedPath $path) -PathType Leaf)) {
        throw "Missing workflow file: $path"
    }
}

$names = @()
foreach ($skill in @($lock.projectSkills) + @($lock.vendorSkills)) {
    if ($skill.name -notmatch '^[a-z0-9]+(-[a-z0-9]+)*$' -or $names -contains $skill.name) {
        throw "Invalid or duplicate skill name: $($skill.name)"
    }
    $names += $skill.name
    $skillPath = Get-CheckedPath ('.agents/skills/' + $skill.name + '/SKILL.md')
    if (-not (Test-Path -LiteralPath $skillPath -PathType Leaf)) {
        throw "Missing skill entry point: $($skill.name)"
    }
}
foreach ($skill in $lock.projectSkills) {
    $metadata = Get-CheckedPath ('.agents/skills/' + $skill.name + '/agents/openai.yaml')
    if (-not (Test-Path -LiteralPath $metadata -PathType Leaf)) {
        throw "Missing skill UI metadata: $($skill.name)"
    }
}

$verified = 0
foreach ($skill in $lock.vendorSkills) {
    if ($skill.ref -notmatch '^[a-f0-9]{40}$' -or @($skill.files).Count -eq 0) {
        throw "Unpinned or empty vendor skill: $($skill.name)"
    }
    $expectedPrefix = '.agents/skills/' + $skill.name + '/'
    $expectedPaths = @()
    foreach ($file in $skill.files) {
        if (-not $file.path.StartsWith($expectedPrefix, [System.StringComparison]::Ordinal) -or
            $file.sha256 -notmatch '^[a-f0-9]{64}$' -or $expectedPaths -contains $file.path) {
            throw "Invalid vendor file entry: $($file.path)"
        }
        $expectedPaths += $file.path
        $path = Get-CheckedPath $file.path
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing vendor file: $($file.path)" }
        if ((Get-Sha256 $path) -ne $file.sha256) {
            throw "Vendor hash mismatch: $($file.path)"
        }
        $verified++
    }
    foreach ($required in @('SKILL.md', 'LICENSE.txt')) {
        if ($expectedPaths -notcontains ($expectedPrefix + $required)) {
            throw "Untracked vendor entry point/license: $($skill.name)"
        }
    }
    $directory = Get-CheckedPath ('.agents/skills/' + $skill.name)
    $actualPaths = @(Get-ChildItem -LiteralPath $directory -Recurse -File | ForEach-Object {
        $_.FullName.Substring($prefix.Length).Replace('\', '/')
    })
    if (@(Compare-Object $expectedPaths $actualPaths).Count -ne 0) {
        throw "Unregistered files in vendor skill: $($skill.name)"
    }
}

Write-Host "OK: $($names.Count) skills present; $verified vendor files match pinned SHA-256 hashes."
Write-Host "This checks installation integrity, not model behavior or Android runtime correctness."
