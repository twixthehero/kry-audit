package com.krythera.audit.db

import com.krythera.audit.events.AuditEvent
import net.minecraft.util.math.BlockPos
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
            this[TableBlockEvents.x] = it.x
            this[TableBlockEvents.y] = it.y
            this[TableBlockEvents.z] = it.z
            this[TableBlockEvents.metadata] = it.metadata
        }

        commit()
    }

    fun query(position: BlockPos, eventTypes: Set<AuditEvent>): List<BlockEvt> {
        val results = mutableListOf<BlockEvt>()

        transaction(database) {

            TableBlockEvents.select {
                TableBlockEvents.x eq position.x
                TableBlockEvents.y eq position.y
                TableBlockEvents.z eq position.z
                TableBlockEvents.eventType inList eventTypes
            }.forEach {
                results.add(
                    BlockEvt(
                        it[TableBlockEvents.blockEventId],
                        it[TableBlockEvents.timestamp],
                        it[TableBlockEvents.eventType],
                        it[TableBlockEvents.x],
                        it[TableBlockEvents.y],
                        it[TableBlockEvents.z],
                        it[TableBlockEvents.metadata]
                    )
                )
            }
        }

        return results
    }

    fun query(startPos: BlockPos, endPos: BlockPos, eventTypes: Set<AuditEvent>): List<BlockEvt> {
        val results = mutableListOf<BlockEvt>()

        transaction(database) {
            TableBlockEvents.select {
                TableBlockEvents.x greaterEq startPos.x
                TableBlockEvents.y greaterEq startPos.y
                TableBlockEvents.z greaterEq startPos.z
                TableBlockEvents.x lessEq endPos.x
                TableBlockEvents.y lessEq endPos.y
                TableBlockEvents.z lessEq endPos.z
                TableBlockEvents.eventType inList eventTypes
            }.forEach {
                results.add(
                    BlockEvt(
                        it[TableBlockEvents.blockEventId],
                        it[TableBlockEvents.timestamp],
                        it[TableBlockEvents.eventType],
                        it[TableBlockEvents.x],
                        it[TableBlockEvents.y],
                        it[TableBlockEvents.z],
                        it[TableBlockEvents.metadata]
                    )
                )
            }
        }

        return results
    }

    private companion object {
        private const val VERSION = 2
        private const val DRIVER = "org.h2.Driver"

        private val LOGGER = LogManager.getLogger(Db::class.java.name)
    }
}