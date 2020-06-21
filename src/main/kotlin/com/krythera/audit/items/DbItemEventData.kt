package com.krythera.audit.items

import com.krythera.audit.db.AuditEvent
import com.krythera.audit.db.TableItemEvents
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class DbItemEventData(private val database: Database) {
    fun addEvents(eventData: List<ItemEventData>) = transaction(database) {
        try {
            TableItemEvents.batchInsert(eventData) {
                this[TableItemEvents.itemEventId] = it.itemEventId
                this[TableItemEvents.timestamp] = it.timestamp
                this[TableItemEvents.eventType] = it.eventType
                this[TableItemEvents.x] = it.x
                this[TableItemEvents.y] = it.y
                this[TableItemEvents.z] = it.z
                this[TableItemEvents.itemId] = it.itemId
                this[TableItemEvents.throwerId] = it.throwerId
                this[TableItemEvents.metadata] = it.metadata
            }

            commit()
        } catch (e: Exception) {
            LOGGER.error(e)
        }
    }

    fun query(
        startX: Double,
        startY: Double,
        startZ: Double,
        endX: Double,
        endY: Double,
        endZ: Double,
        eventTypes: Set<AuditEvent>
    ): List<ItemEventData> {
        val results = mutableListOf<ItemEventData>()

        transaction(database) {
            TableItemEvents.select {
                (TableItemEvents.x greaterEq startX) and
                        (TableItemEvents.y greaterEq startY) and
                        (TableItemEvents.z greaterEq startZ) and
                        (TableItemEvents.x lessEq endX) and
                        (TableItemEvents.y lessEq endY) and
                        (TableItemEvents.z lessEq endZ) and
                        (TableItemEvents.eventType inList eventTypes)
            }.forEach {
                results.add(
                    ItemEventData(
                        it[TableItemEvents.itemEventId],
                        it[TableItemEvents.timestamp],
                        it[TableItemEvents.eventType],
                        it[TableItemEvents.x],
                        it[TableItemEvents.y],
                        it[TableItemEvents.z],
                        it[TableItemEvents.itemId],
                        it[TableItemEvents.throwerId],
                        it[TableItemEvents.metadata]
                    )
                )
            }
        }

        return results
    }

    private companion object {
        private val LOGGER = LogManager.getLogger(DbItemEventData::class.java)
    }
}