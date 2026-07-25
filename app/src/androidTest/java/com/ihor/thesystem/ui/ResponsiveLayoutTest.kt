package com.ihor.thesystem.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.TheSystemTheme
import com.ihor.thesystem.core.ui.SystemUiTestTags
import com.ihor.thesystem.core.ui.components.SystemBottomNavBar
import com.ihor.thesystem.core.ui.components.SystemSectionTitle
import com.ihor.thesystem.core.ui.components.SystemSettingsRow
import com.ihor.thesystem.feature.status.ui.RpgStatusDashboard
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData
import com.ihor.thesystem.feature.status.viewmodel.TodayOrderUiModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResponsiveLayoutTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun smallPhone_todayOrderCtaIsReachable_andVerticalScrollKeepsActionsMode() {
        setCompactStatusDashboard()

        composeRule.onAllNodesWithTag(SystemUiTestTags.STATUS_ACTIONS_CONTENT).assertCountEquals(1)
        composeRule.onNodeWithTag(SystemUiTestTags.TODAY_ORDER_CTA)
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.onNodeWithTag(SystemUiTestTags.STATUS_ACTIONS_CONTENT)
            .performTouchInput { swipeUp() }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(SystemUiTestTags.STATUS_ACTIONS_CONTENT).assertCountEquals(1)
        composeRule.onAllNodesWithTag(SystemUiTestTags.STATUS_INFO_CONTENT).assertCountEquals(0)
    }

    @Test
    fun smallPhone_clearlyHorizontalSwipeSwitchesStatusMode() {
        setCompactStatusDashboard()

        composeRule.onNodeWithTag(SystemUiTestTags.STATUS_DASHBOARD)
            .performTouchInput { swipeLeft() }

        composeRule.waitUntil(timeoutMillis = 2_000) {
            composeRule.onAllNodesWithTag(SystemUiTestTags.STATUS_INFO_CONTENT)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithTag(SystemUiTestTags.STATUS_INFO_CONTENT).assertCountEquals(1)
    }

    @Test
    fun bottomNavigation_switchesAllFiveTopLevelTabs() {
        composeRule.setContent {
            TheSystemTheme {
                val navController = rememberNavController()
                Column(modifier = Modifier.requiredSize(width = 360.dp, height = 640.dp)) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Status,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable<Routes.Status> { RouteContent(ROUTE_STATUS) }
                        composable<Routes.Calendar> { RouteContent(ROUTE_CALENDAR) }
                        composable<Routes.Cycle> { RouteContent(ROUTE_SYSTEM) }
                        composable<Routes.Statistics> { RouteContent(ROUTE_STATISTICS) }
                        composable<Routes.Profile> { RouteContent(ROUTE_PROFILE) }
                    }
                    SystemBottomNavBar(navController = navController)
                }
            }
        }

        assertTabSwitch(SystemUiTestTags.BOTTOM_NAV_CALENDAR, ROUTE_CALENDAR)
        assertTabSwitch(SystemUiTestTags.BOTTOM_NAV_SYSTEM, ROUTE_SYSTEM)
        assertTabSwitch(SystemUiTestTags.BOTTOM_NAV_STATISTICS, ROUTE_STATISTICS)
        assertTabSwitch(SystemUiTestTags.BOTTOM_NAV_PROFILE, ROUTE_PROFILE)
        assertTabSwitch(SystemUiTestTags.BOTTOM_NAV_STATUS, ROUTE_STATUS)
    }

    @Test
    fun representativeSharedText_isVisibleAtFontScaleOnePointThree() {
        val sectionTitle = "Training and calendar cycle settings"
        val settingsTitle = "Training and calendar preferences"
        val subtitle = "Cycle, exercises, equipment, backup and restore preferences"

        composeRule.setContent {
            TheSystemTheme {
                FontScale(1.3f) {
                    Column(modifier = Modifier.requiredSize(width = 360.dp, height = 240.dp)) {
                        SystemSectionTitle(title = sectionTitle)
                        SystemSettingsRow(
                            title = settingsTitle,
                            subtitle = subtitle,
                            onClick = {}
                        )
                    }
                }
            }
        }

        composeRule.onNodeWithText(sectionTitle, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText(settingsTitle, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText(subtitle, useUnmergedTree = true).assertIsDisplayed()
    }

    private fun setCompactStatusDashboard() {
        composeRule.setContent {
            TheSystemTheme {
                FontScale(1.3f) {
                    Box(modifier = Modifier.requiredSize(width = 360.dp, height = 640.dp)) {
                        RpgStatusDashboard(
                            data = StatusUiData(
                                todayOrder = TodayOrderUiModel(
                                    title = "Workout A",
                                    reason = "The system selected the main workout based on plan and recovery.",
                                    primaryActionLabel = "Start workout",
                                    outcomeText = "+75 XP",
                                    durationText = "45 min",
                                    readinessProgress = 0.85f,
                                    actionEnabled = true
                                )
                            ),
                            onAvatarSelected = {},
                            onEditNameTap = {},
                            onStartWorkout = {},
                            onOpenCalendar = {},
                            onOpenWorkoutSettings = {},
                            onTaskToggled = {},
                            onAddTask = {},
                            onAddMicrotask = {},
                            onTodosReordered = {},
                            onRemoveTask = {}
                        )
                    }
                }
            }
        }
    }

    private fun assertTabSwitch(tabTag: String, routeTag: String) {
        composeRule.onNodeWithTag(tabTag).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(routeTag).assertIsDisplayed()
    }

    private companion object {
        const val ROUTE_STATUS = "route_status"
        const val ROUTE_CALENDAR = "route_calendar"
        const val ROUTE_SYSTEM = "route_system"
        const val ROUTE_STATISTICS = "route_statistics"
        const val ROUTE_PROFILE = "route_profile"
    }
}

@Composable
private fun FontScale(fontScale: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density = density.density, fontScale = fontScale),
        content = content
    )
}

@Composable
private fun RouteContent(tag: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(tag)
    )
}
