package com.krythera.audit.db

import com.krythera.audit.events.AuditEvent
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class Db(dimensionDir: File) {
    private var database: Database

    init {
        val dbFile = "${dimensionDir.toString().replace('\\', '/')}/data/kryaudit"

        database = Database.connect("jdbc:h2:$dbFile", DRIVER)

        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(TableDbVersion)
            SchemaUtils.createMissingTablesAndColumns(TableBlockEvents)
        }

        // if db has been updated, need to roll to another db file
        val currentVersion = getDbVersion()
        if (VERSION != currentVersion) {
            LOGGER.info("Old database version found ($currentVersion), rolling to new version file ($VERSION)")

            Files.deleteIfExists(Paths.get("$dbFile.trace.db"))

            val oldVersionFile = "$dbFile.mv.db.v$currentVersion"
            Files.move(Paths.get("$dbFile.mv.db"), Paths.get(oldVersionFile))

            // recreate db
            database = Database.connect("jdbc:h2:$dbFile", DRIVER)

            // set version in new db
            transaction(database) {
                SchemaUtils.createMissingTablesAndColumns(TableDbVersion)
                SchemaUtils.createMissingTablesAndColumns(TableBlockEvents)

                TableDbVersion.insert {
                    it[version] = VERSION
                }

                commit()
            }
        }
    }

    private fun getDbVersion(): Int {
        var version: Int? = null

        transaction(database) {
            version = TableDbVersion.selectAll().firstOrNull()?.get(TableDbVersion.version)
        }

        return version ?: 0
    }

    fun addEvents(events: List<BlockEvt>) = transaction(database) {
        TableBlockEvents.batchInsert(events) {
            this[TableBlockEvents.blockEventId] = it.blockEventId
            this[TableBlockEvents.timestamp] = it.timestamp
            this[TableBlockEvents.eventType] = it.eventType
            this[TableBlockEvents.blockPos] = it.blockPos
            this[TableBlockEvents.metadata] = it.metadata
        }

        commit()
    }

    fun query(position: Long, eventTypes: Set<AuditEvent>): List<BlockEvt> {
        val results = mutableListOf<BlockEvt>()

        transaction(database) {
            TableBlockEvents.select {
                TableBlockEvents.blockPos eq position
                TableBlockEvents.eventType inList eventTypes
            }.forEach {
                val blockEvt = BlockEvt(
                    it[TableBlockEvents.blockEventId],
                    it[TableBlockEvents.timestamp],
                    it[TableBlockEvents.eventType],
                    it[TableBlockEvents.blockPos],
                    it[TableBlockEvents.metadata]
                )
                results.add(blockEvt)
            }
        }

        return results
    }

    private companion object {
        private const val VERSION = 1
        private const val DRIVER = "org.h2.Driver"

        private val LOGGER = LogManager.getLogger(Db::class.java.name)
    }
}