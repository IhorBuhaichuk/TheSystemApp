package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DomainModuleBoundaryGuardTest {

    @Test
    fun `app module does not own domain production sources`() {
        val repoRoot = repoRoot()
        val appDomainDir = repoRoot.resolve("app/src/main/java/com/ihor/thesystem/domain")
        val leakedFiles = appDomainDir
            .takeIf { it.exists() }
            ?.walkTopDown()
            ?.filter { it.isFile && it.extension == "kt" }
            ?.map { it.relativeTo(repoRoot).invariantSeparatorsPath }
            ?.toList()
            .orEmpty()

        assertTrue(
            "Production domain sources must live in :domain, not :app: $leakedFiles",
            leakedFiles.isEmpty()
        )
    }

    @Test
    fun `domain module does not depend on android app or provider sdk packages`() {
        val repoRoot = repoRoot()
        val domainRoot = repoRoot.resolve("domain/src/main/java")
        val forbiddenImports = listOf(
            "import android.",
            "import androidx.",
            "import com.google.ai.",
            "import com.ihor.thesystem.R",
            "import com.ihor.thesystem.BuildConfig",
            "import com.ihor.thesystem.core.ui.",
            "import com.ihor.thesystem.core.util.",
            "import com.ihor.thesystem.data.",
            "import com.ihor.thesystem.feature.",
            "import timber.log."
        )

        val offenders = domainRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    forbiddenImports.firstOrNull { line.startsWith(it) }?.let {
                        "${file.relativeTo(repoRoot).invariantSeparatorsPath}:${index + 1} -> $line"
                    }
                }
            }
            .toList()

        assertTrue(
            "Domain module must stay framework and app independent: $offenders",
            offenders.isEmpty()
        )
    }

    private fun repoRoot(): File {
        val appRoot = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        return requireNotNull(appRoot.parentFile)
    }
}
