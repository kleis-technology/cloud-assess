package org.cloud_assess

import org.apache.commons.compress.archivers.examples.Expander
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.isRegularFile

@SpringBootApplication
class CloudAssessApplication(
    @param:Value("\${lca.directory}") private val workingDir: Path,
    @param:Value("\${lca.inventory.directory}") private val inventorySubdir: Path,
    @param:Value("\${lca.archive.directory}") private val archiveSubdir: Path,
    @param:Value("\${lca.inventory.zip:#{null}}") private val inventoryZipFile: Path?,
) : CommandLineRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        inventoryZipFile
            ?.let { workingDir / archiveSubdir / it }
            ?.let {
                if (it.isRegularFile()) it else {
                    logger.info("File ${it.toAbsolutePath()} is not a regular file")
                    null
                }
            }
            ?.let {
                logger.info("Extracting `$it` to ${workingDir / inventorySubdir}")
                Expander().expand(it, workingDir / inventorySubdir)
            }
    }
}

fun main(args: Array<String>) {
    runApplication<CloudAssessApplication>(*args)
}
