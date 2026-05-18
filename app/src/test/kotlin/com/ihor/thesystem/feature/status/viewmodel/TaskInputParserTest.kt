package com.ihor.thesystem.feature.status.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskInputParserTest {

    @Test
    fun `splits multiline task input into separate task names`() {
        val input = """
            Buy food
            Read plan

            Train
        """.trimIndent()

        assertEquals(
            listOf("Buy food", "Read plan", "Train"),
            input.toTaskNames()
        )
    }

    @Test
    fun `removes common list markers from task names`() {
        val input = """
            - First task
            * Second task
            1. Third task
            2) Fourth task
            [ ] Fifth task
            [x] Sixth task
        """.trimIndent()

        assertEquals(
            listOf(
                "First task",
                "Second task",
                "Third task",
                "Fourth task",
                "Fifth task",
                "Sixth task"
            ),
            input.toTaskNames()
        )
    }
}
