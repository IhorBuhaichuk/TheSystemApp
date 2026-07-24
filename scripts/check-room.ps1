$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$androidStudioJbr = "C:\Program Files\Android\Android Studio\jbr"
if (Test-Path $androidStudioJbr) {
    $env:JAVA_HOME = $androidStudioJbr
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}

.\gradlew.bat :app:testDebugUnitTest `
    --tests "com.ihor.thesystem.data.local.room.database.RoomSchemaGuardTest" `
    --tests "com.ihor.thesystem.data.local.room.database.DatabasePopulatorGuardTest" `
    --tests "com.ihor.thesystem.data.local.room.database.DatabasePopulatorCoreMetadataTest" `
    --tests "com.ihor.thesystem.data.local.room.database.WorkoutAnalyticsQueryGuardTest"
