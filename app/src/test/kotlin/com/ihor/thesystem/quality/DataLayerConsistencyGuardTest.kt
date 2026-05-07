package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DataLayerConsistencyGuardTest {

    @Test
    fun `room entities do not create timestamps directly`() {
        val projectRoot = File(requireNotNull(System.getProperty("user.dir")))
        val entityRoot = projectRoot.resolve("src/main/java/com/ihor/thesystem/data/local/room/entity")

        val offenders = entityRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                val path = file.relativeTo(projectRoot).invariantSeparatorsPath
                file.readLines().mapIndexedNotNull { index, line ->
                    if ("System.currentTimeMillis()" in line) {
                        "$path:${index + 1}"
                    } else {
                        null
                    }
                }
            }
            .toList()

        assertTrue(
            "Room entities must receive timestamps from repositories/use cases via AppClock: $offenders",
            offenders.isEmpty()
        )
    }
}
