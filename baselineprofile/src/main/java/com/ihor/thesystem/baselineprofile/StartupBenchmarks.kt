package com.ihor.thesystem.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingGfxInfoMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingLegacyMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@OptIn(ExperimentalMetricApi::class)
class StartupBenchmarks {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartupWithoutCompilation() {
        measureColdStartup(CompilationMode.None())
    }

    @Test
    fun coldStartupWithBaselineProfile() {
        measureColdStartup(
            CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            )
        )
    }

    @Test
    fun statisticsNavigationWithoutCompilation() {
        measureStatisticsNavigation(CompilationMode.None())
    }

    @Test
    fun statisticsNavigationWithBaselineProfile() {
        measureStatisticsNavigation(
            CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            )
        )
    }

    @Test
    fun profileNavigationWithoutCompilation() {
        measureProfileNavigation(CompilationMode.None())
    }

    @Test
    fun profileNavigationWithBaselineProfile() {
        measureProfileNavigation(
            CompilationMode.Partial(
                baselineProfileMode = BaselineProfileMode.Require
            )
        )
    }

    private fun measureStatisticsNavigation(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingGfxInfoMetric()),
            compilationMode = compilationMode,
            iterations = 5,
            setupBlock = {
                pressHome()
                killProcess()
                startActivityAndWait()
                ensureReturningUser()
            },
            measureBlock = {
                openStatistics()
            }
        )
    }

    private fun measureColdStartup(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingLegacyMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.COLD,
            iterations = 5,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                ensureReturningUser()
            },
            measureBlock = {
                startActivityAndWait()
            }
        )
    }

    private fun measureProfileNavigation(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingGfxInfoMetric()),
            compilationMode = compilationMode,
            iterations = 5,
            setupBlock = {
                pressHome()
                killProcess()
                startActivityAndWait()
                ensureReturningUser()
            },
            measureBlock = {
                openProfile()
            }
        )
    }
}
