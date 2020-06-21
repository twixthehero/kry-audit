package com.krythera.audit.items

import com.krythera.audit.db.AuditEvent
import net.minecraftforge.event.entity.item.ItemExpireEvent
import net.minecraftforge.event.entity.item.ItemTossEvent
import net.minecraftforge.eventbus.api.Event
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Database
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

class ItemEventLogger(private val dimensionId: Int, database: Database) : Runnable {
    private val queue = ConcurrentLinkedQueue<Event>()

    val db = DbItemEventData(database)
    private var nextId: Byte = 1

    private var shouldShutdown = false

    fun add(e: Event) {
        queue.add(e)
    }

    fun shutdown() {
        LOGGER.debug("[dim $dimensionId] marking shutdown flag")
        shouldShutdown = true
    }

    @ExperimentalUnsignedTypes
    override fun run() {
        LOGGER.debug("[dim $dimensionId] logger started")

        while (!shouldShutdown) {
            val events = mutableListOf<ItemEventData>()
            for (i in 0 until queue.size.coerceAtMost(15)) {
                val e = queue.poll() ?: break
                LOGGER.debug("[dim $dimensionId] adding event: $e")

                when (e) {
                    is ItemTossEvent -> {
                        LOGGER.debug("toss pos: (${e.entity.posX}, ${e.entity.posY}, ${e.entity.posZ})")

                        events.add(
                            ItemEventData(
                                getNextId(),
                                Instant.now(),
                                AuditEvent.TOSS,
                                e.entity.posX,
                                e.entity.posY,
                                e.entity.posZ,
                                e.entityItem.item.item.registryName?.toString(),
                                e.entityItem.throwerId,
                                ByteArray(0)
                            )
                        )
                    }
                    is ItemExpireEvent -> {
                        LOGGER.debug("expire pos: (${e.entity.posX}, ${e.entity.posY}, ${e.entity.posZ})")

                        events.add(
                            ItemEventData(
                                getNextId(),
                                Instant.now(),
                                AuditEvent.EXPIRE,
                                e.entity.posX,
                                e.entity.posY,
                                e.entity.posZ,
                                e.entityItem.item.item.registryName?.toString(),
                                e.entityItem.throwerId,
                                ByteArray(0)
                            )
                        )
                    }
                    else -> {
                        LOGGER.error("invalid event type: $e")
                    }
                }
            }

            if (events.isNotEmpty()) {
                LOGGER.debug("[dim $dimensionId] writing ${events.size} events")
                db.addEvents(events)
            }
        }

        LOGGER.debug("[dim $dimensionId] stopped")
    }

    private fun getNextId(): Byte {
        val id = nextId++
        nextId = (nextId % Byte.MAX_VALUE).toByte()
        return id
    }

    private companion object {
        private val LOGGER = LogManager.getLogger(ItemEventLogger::class.java)
    }
}