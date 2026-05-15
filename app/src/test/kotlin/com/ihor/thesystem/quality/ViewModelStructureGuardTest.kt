package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ViewModelStructureGuardTest {

    @Test
    fun `ui state files do not own viewmodels or ui text mappers`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val sourceRoot = projectRoot.resolve("src/main")
        val forbiddenDeclarations = listOf(
            Regex("""@HiltViewModel\b"""),
            Regex("""\bclass\s+\w*ViewModel\b"""),
            Regex("""\bobject\s+\w*UiTextMapper\b""")
        )

        val offenders = sourceRoot.walkTopDown()
            .filter { it.isFile && it.name.endsWith("UiState.kt") }
            .flatMap { file ->
                val path = file.relativeTo(projectRoot).invariantSeparatorsPath
                file.readLines().mapIndexedNotNull { index, line ->
                    if (forbiddenDeclarations.any { it.containsMatchIn(line) }) {
                        "$path:${index + 1}"
                    } else {
                        null
                    }
                }
            }
            .toList()

        assertTrue(
            "Keep UI state files declarative; move ViewModels and mappers into dedicated files: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `viewmodel ui state messages use UiText instead of raw nullable strings`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val featureRoot = projectRoot.resolve("src/main/java/com/ihor/thesystem/feature")
        val rawMessageProperties = listOf(
            Regex("""\bmessage:\s*String\?"""),
            Regex("""\berrorMessage:\s*String\?""")
        )

        val offenders = featureRoot.walkTopDown()
            .filter { it.isFile && it.name.endsWith("ViewModel.kt") }
            .flatMap { file ->
                val path = file.relativeTo(projectRoot).invariantSeparatorsPath
                file.readLines().mapIndexedNotNull { index, line ->
                    if (rawMessageProperties.any { it.containsMatchIn(line) }) {
                        "$path:${index + 1}"
                    } else {
                        null
                    }
                }
            }
            .toList()

        assertTrue(
            "ViewModel UI messages should be UiText/StringResource-ready, not raw String?: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `feature viewmodels use dispatcher provider instead of hardcoded dispatchers`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val featureRoot = projectRoot.resolve("src/main/java/com/ihor/thesystem/feature")
        val hardcodedDispatchers = listOf(
            "Dispatchers.IO",
            "Dispatchers.Default",
            "Dispatchers.Main"
        )

        val offenders = featureRoot.walkTopDown()
            .filter { it.isFile && it.name.endsWith("ViewModel.kt") }
            .flatMap { file ->
                val path = file.relativeTo(projectRoot).invariantSeparatorsPath
                file.readLines().mapIndexedNotNull { index, line ->
                    hardcodedDispatchers.firstOrNull { it in line }?.let { dispatcher ->
                        "$path:${index + 1} uses $dispatcher"
                    }
                }
            }
            .toList()

        assertTrue(
            "Inject DispatcherProvider in feature ViewModels instead of hardcoding coroutine dispatchers: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun `statistics viewmodel depends on use cases instead of repositories`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val file = projectRoot.resolve(
            "src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt"
        )
        val source = file.readText()
        val forbidden = listOf(
            "domain.repository.",
            "ProgressionMatrixRepository",
            "ViewingDateRepository"
        ).filter { it in source }

        assertTrue(
            "StatisticsViewModel should keep orchestration in domain use cases: $forbidden",
            forbidden.isEmpty()
        )
    }

    @Test
    fun `status viewmodel delegates day synchronization decisions to domain`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val source = projectRoot
            .resolve("src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusViewModel.kt")
            .readText()
        val forbidden = listOf(
            "hasNoQuests",
            "dateChanged",
            "lastInitEpochDay <",
            "needsDailyInit =="
        ).filter { it in source }

        assertTrue(
            "StatusViewModel must not decide when to finalize/sync the day; call SyncTodayStateUseCase: $forbidden",
            forbidden.isEmpty()
        )
        assertTrue(
            "StatusViewModel should call SyncTodayStateUseCase through StatusUseCases.",
            "useCases.syncTodayState()" in source
        )
    }

    @Test
    fun `status viewmodel routes top level todos outside quest tasks`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val source = projectRoot
            .resolve("src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusViewModel.kt")
            .readText()
        val methodBody = Regex(
            pattern = """fun onAddTaskConfirmed\(questId: Int, taskName: String\).*?\{([\s\S]*?)\n    \}"""
        ).find(source)?.groupValues?.get(1).orEmpty()

        assertTrue(
            "Top-level To-do additions must use AddTodayTodoUseCase when there is no quest id.",
            "questId > 0" in methodBody && "useCases.addTodayTodo(taskName)" in methodBody
        )
        assertTrue(
            "Quest task additions should still use AddTaskToQuestUseCase for real quest ids.",
            "useCases.addTaskToQuest(questId, taskName)" in methodBody
        )
    }

    @Test
    fun `workout report opens analysis for the saved session`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val routeSource = projectRoot
            .resolve("src/main/java/com/ihor/thesystem/core/navigation/Routes.kt")
            .readText()
        val hostSource = projectRoot
            .resolve("src/main/java/com/ihor/thesystem/feature/status/ui/WorkoutDialogHost.kt")
            .readText()
        val cycleSource = projectRoot
            .resolve("src/main/java/com/ihor/thesystem/feature/cycle/ui/CycleScreen.kt")
            .readText()
        val viewModelSource = projectRoot
            .resolve("src/main/java/com/ihor/thesystem/feature/architect/viewmodel/WorkoutAnalysisViewModel.kt")
            .readText()

        assertTrue(
            "WorkoutAnalysis route must carry the saved workout session id.",
            "data class WorkoutAnalysis(val sessionId: Long = 0L)" in routeSource
        )
        assertTrue(
            "WorkoutReportDialog must pass the saved report session id into navigation.",
            "onOpenWorkoutAnalysis(dialogState.report.sessionId)" in hostSource
        )
        assertTrue(
            "Cycle screen must navigate to analysis with the report session id.",
            "Routes.WorkoutAnalysis(sessionId = sessionId)" in cycleSource
        )
        assertTrue(
            "WorkoutAnalysisViewModel must read the route session id before loading analysis.",
            "savedStateHandle.get<Long>(SESSION_ID_ARG)" in viewModelSource &&
                "private const val SESSION_ID_ARG = \"sessionId\"" in viewModelSource
        )
    }
}
