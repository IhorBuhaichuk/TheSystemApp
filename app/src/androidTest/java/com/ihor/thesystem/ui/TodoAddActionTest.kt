package com.ihor.thesystem.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ihor.thesystem.core.theme.TheSystemTheme
import com.ihor.thesystem.feature.status.ui.TodoBlock
import com.ihor.thesystem.feature.status.viewmodel.TodoUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodoAddActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bothAddControlsInvokeTheSameActionWithAnEmptyList() = verifyAddControls(0)

    @Test
    fun bothAddControlsInvokeTheSameActionWithExistingTodos() = verifyAddControls(3)

    private fun verifyAddControls(count: Int) {
        var additions = 0
        composeRule.setContent {
            TheSystemTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TodoBlock(
                        todos = List(count) { TodoUiModel(it + 100, "Task $it", false) },
                        onTaskToggled = {},
                        onAddTask = { additions++ },
                        onAddMicrotask = {},
                        onTodosReordered = {},
                        onRemoveTask = {}
                    )
                }
            }
        }
        composeRule.onNodeWithContentDescription("Додати завдання").performClick()
        composeRule.runOnIdle { assertEquals(1, additions) }
        composeRule.onNodeWithText("Додати завдання").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals(2, additions) }
    }
}
