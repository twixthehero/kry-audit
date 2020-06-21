package com.krythera.audit.blocks

import com.krythera.audit.db.AuditEvent
import com.krythera.audit.db.TableBlockEvents
import net.minecraft.util.math.BlockPos
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class DbBlockEventData(private val database: Database) {
    fun addEvents(eventData: List<BlockEventData>) = transaction(database) {
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
    }

    fun query(position: BlockPos, eventTypes: Set<AuditEvent>): List<BlockEventData> {
        val results = mutableListOf<BlockEventData>()

        transaction(database) {

            TableBlockEvents.select {
                TableBlockEvents.x eq position.x
                TableBlockEvents.y eq position.y
                TableBlockEvents.z eq position.z
                TableBlockEvents.eventType inList eventTypes
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
                TableBlockEvents.x greaterEq startPos.x
                TableBlockEvents.y greaterEq startPos.y
                TableBlockEvents.z greaterEq startPos.z
                TableBlockEvents.x lessEq endPos.x
                TableBlockEvents.y lessEq endPos.y
                TableBlockEvents.z lessEq endPos.z
                TableBlockEvents.eventType inList eventTypes
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
}