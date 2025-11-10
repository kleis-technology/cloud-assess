package org.cloud_assess.config

import ch.kleis.lcaac.core.config.CacheConfig
import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.cache.SourceOpsCache
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorKeys
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.isRegularFile

@Configuration
class LcaConfig {
    @Bean
    fun symbolTable(
        @Value("\${lca.directory}") workingDir: Path,
        @Value("\${lca.lib.directory}") libSubdir: Path,
    ): SymbolTable<BasicNumber> {
        val libDir = workingDir / libSubdir
        val files = Files.walk(libDir)
            .filter {
                it.isRegularFile() && it.fileName.toString().endsWith(".lca")
            }
            .map {
                val lexer = LcaLangLexer(CharStreams.fromStream(it.toFile().inputStream()))
                val tokens = CommonTokenStream(lexer)
                val parser = LcaLangParser(tokens)
                val lcaFile = parser.lcaFile()
                lcaFile
            }
            .toList()
            .asSequence()
        val loader = Loader(BasicOperations)
        return loader.load(files, listOf(LoaderOption.WITH_PRELUDE))
    }

    @Bean
    fun lcaacConfig(
        @Value("\${lca.directory}") workingDir: Path,
        @Value("\${lca.inventory.directory}") inventorySubdir: Path,
    ): LcaacConfig {
        val config = LcaacConfig(
            name = "Cloud Assess",
            description = "Cloud Assess default LCAAC configuration",
            connectors = listOf(
                ConnectorConfig(
                    name = CsvConnectorKeys.CSV_CONNECTOR_NAME,
                    cache = CacheConfig(
                        enabled = false,
                        maxSize = 1024,
                    ),
                    options = mapOf(
                        CsvConnectorKeys.CSV_CONNECTOR_KEY_DIRECTORY to inventorySubdir.toString(),
                    )
                )
            ),
        )
        return config
    }

    @Bean
    fun connectorFactory(
        lcaacConfig: LcaacConfig,
        @Value("\${lca.directory}") workingDir: File,
        symbolTable: SymbolTable<BasicNumber>,
    ): ConnectorFactory<BasicNumber> {
        return ConnectorFactory(
            workingDirectory = workingDir.path,
            lcaacConfig = lcaacConfig,
            ops = BasicOperations,
            symbolTable = symbolTable,
        )
    }

    @Bean
    fun defaultConnectors(
        factory: ConnectorFactory<BasicNumber>
    ): Map<String, DataSourceConnector<BasicNumber>> = factory.buildConnectors()

    @Bean
    fun defaultConnectorsCache(
        defaultConnectors: Map<String, DataSourceConnector<BasicNumber>>,
    ): Map<String, SourceOpsCache<BasicNumber>> = defaultConnectors
        .filter { it.value.getConfig().cache.enabled }
        .mapValues {
            SourceOpsCache(
                it.value.getConfig().cache.maxSize,
                it.value.getConfig().cache.maxRecordsPerCacheLine,
            )
        }

    @Bean
    fun defaultDataSourceOperations(
        lcaacConfig: LcaacConfig,
        defaultConnectors: Map<String, DataSourceConnector<BasicNumber>>,
        defaultConnectorsCache: Map<String, SourceOpsCache<BasicNumber>>,
    ): DefaultDataSourceOperations<BasicNumber> {
        return DefaultDataSourceOperations(
            ops = BasicOperations,
            config = lcaacConfig,
            connectors = defaultConnectors,
            cache = defaultConnectorsCache,
        )
    }
}
