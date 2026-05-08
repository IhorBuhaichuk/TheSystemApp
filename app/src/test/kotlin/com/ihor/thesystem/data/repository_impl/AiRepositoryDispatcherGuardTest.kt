package com.ihor.thesystem.data.repository_impl

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AiRepositoryDispatcherGuardTest {

    @Test
    fun `ai repositories use injected dispatcher provider`() {
        val files = listOf(
            "AiArchitectRepositoryImpl.kt",
            "LiveCoachRepositoryImpl.kt"
        ).map { fileName ->
            projectRoot()
                .resolve("src/main/java/com/ihor/thesystem/data/repository_impl")
                .resolve(fileName)
        }

        files.forEach { file ->
            val source = file.readText()
            assertFalse(
                "${file.name} must not hardcode Dispatchers.IO.",
                "Dispatchers.IO" in source
            )
            assertTrue(
                "${file.name} must use DispatcherProvider for IO work.",
                "DispatcherProvider" in source && "withContext(dispatchers.io)" in source
            )
        }
    }

    private fun projectRoot(): File =
        File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
}
