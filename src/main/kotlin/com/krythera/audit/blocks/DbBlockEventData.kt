package com.krythera.audit.blocks

import com.krythera.audit.db.AuditEvent
import com.krythera.audit.db.TableBlockEvents
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class DbBlockEventData(private val database: Database) {
    fun addEvents(eventData: List<BlockEventData>) = transaction(database) {
        try {
            TableBlockEvents.batchInsert(eventData) {
                this[TableBlockEvents.blockEventId] = it.blockEventId
                this[TableBlockEvents.timestamp] = it.timestamp
                this[TableBlockEvents.eventType] = it.eventType
                this[TableBlockEvents.x] = it.x
                this[TableBlockEvents.y] = it.y
                this[TableBlockEvents.z] = it.z
                this[TableBlockEvents.metadata] = it.metadata
            }

            commit()
        } catch (e: Exception) {
            LOGGER.error(e)
        }
    }

    fun query(position: BlockPos, eventTypes: Set<AuditEvent>): List<BlockEventData> {
        val results = mutableListOf<BlockEventData>()

        transaction(database) {
            TableBlockEvents.select {
                (TableBlockEvents.x eq position.x) and
                        (TableBlockEvents.y eq position.y) and
                        (TableBlockEvents.z eq position.z) and
                        (TableBlockEvents.eventType inList eventTypes)
            }.forEach {
                results.add(
                    BlockEventData(
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

    fun query(
        startPos: BlockPos,
        endPos: BlockPos,
        eventTypes: Set<AuditEvent>
    ): List<BlockEventData> {
        val results = mutableListOf<BlockEventData>()

        transaction(database) {
            TableBlockEvents.select {
                (TableBlockEvents.x greaterEq startPos.x) and
                        (TableBlockEvents.y greaterEq startPos.y) and
                        (TableBlockEvents.z greaterEq startPos.z) and
                        (TableBlockEvents.x lessEq endPos.x) and
                        (TableBlockEvents.y lessEq endPos.y) and
                        (TableBlockEvents.z lessEq endPos.z) and
                        (TableBlockEvents.eventType inList eventTypes)
            }.forEach {
                results.add(
                    BlockEventData(
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
        private val LOGGER = LogManager.getLogger(DbBlockEventData::class.java)
    }
}