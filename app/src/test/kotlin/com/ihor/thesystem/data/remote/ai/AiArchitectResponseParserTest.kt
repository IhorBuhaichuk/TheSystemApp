package com.ihor.thesystem.data.remote.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AiArchitectResponseParserTest {

    private val parser = AiArchitectResponseParser()

    @Test
    fun `extracts balanced json object from fenced response`() {
        val response = """
            ```json
            {
              "feedback_text": "Done",
              "next_workout_targets": []
            }
            ```
            trailing text that should be ignored
        """.trimIndent()

        val result = parser.extractJsonObject(response)

        assertEquals(
            """
            {
              "feedback_text": "Done",
              "next_workout_targets": []
            }
            """.trimIndent(),
            result
        )
    }

    @Test
    fun `extracts json when feedback contains braces inside a string`() {
        val response = """
            prefix {
              "feedback_text": "Keep {core} tight",
              "next_workout_targets": []
            } suffix {"ignored": true}
        """.trimIndent()

        val result = parser.extractJsonObject(response)

        assertEquals(
            """
            {
              "feedback_text": "Keep {core} tight",
              "next_workout_targets": []
            }
            """.trimIndent(),
            result
        )
    }

    @Test
    fun `maps valid targets and drops unsafe directives`() {
        val response = """
            {
              "feedback_text": "Solid session",
              "next_workout_targets": [
                {
                  "exercise_id": 7,
                  "nextWeight": 52.5,
                  "nextSets": 3,
                  "nextReps": "8-10",
                  "aiFeedback": "Progress"
                },
                {
                  "exercise_id": 0,
                  "nextWeight": 80,
                  "nextSets": 3,
                  "nextReps": "8"
                },
                {
                  "exercise_id": 8,
                  "nextWeight": 80,
                  "nextSets": 0,
                  "nextReps": "8"
                }
              ],
              "aiFeedback": "Global note"
            }
        """.trimIndent()

        val result = parser.parse(response)

        assertEquals("Solid session", result.feedbackText)
        assertEquals("Global note", result.aiFeedback)
        assertEquals(1, result.recommendations.size)
        assertEquals(7, result.recommendations.single().exerciseId)
        assertEquals(52.5f, result.recommendations.single().weight, 0.001f)
        assertEquals(3, result.recommendations.single().sets)
        assertEquals("8-10", result.recommendations.single().reps)
    }

    @Test
    fun `throws when response has no json object`() {
        assertThrows(AiMalformedResponseException::class.java) {
            parser.parse("plain text response")
        }
    }

    @Test
    fun `throws when json object is incomplete`() {
        assertThrows(AiMalformedResponseException::class.java) {
            parser.parse("""{"feedback_text":"Done","next_workout_targets":[""")
        }
    }

    @Test
    fun `malformed json cannot become actionable recommendations`() {
        assertThrows(AiMalformedResponseException::class.java) {
            parser.parse(
                """
                {
                  "feedback_text": "Done",
                  "next_workout_targets": [
                    {
                      "exercise_id": 7,
                      "nextWeight": "heavy",
                      "nextSets": 3,
                      "nextReps": "8"
                    }
                  ]
                }
                """.trimIndent()
            )
        }
    }

    @Test
    fun `drops negative weights and blank reps`() {
        val response = """
            {
              "feedback_text": "Done",
              "next_workout_targets": [
                {
                  "exercise_id": 7,
                  "nextWeight": -5,
                  "nextSets": 3,
                  "nextReps": "8"
                },
                {
                  "exercise_id": 8,
                  "nextWeight": 20,
                  "nextSets": 3,
                  "nextReps": " "
                }
              ]
            }
        """.trimIndent()

        val result = parser.parse(response)

        assertEquals(emptyList<Any>(), result.recommendations)
    }
}
