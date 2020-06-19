package com.krythera.audit.events

import com.krythera.audit.KryAudit
import com.krythera.audit.db.Db
import com.krythera.audit.db.Evt
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.eventbus.api.Event
import org.apache.logging.log4j.LogManager
import java.io.File
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

class BlockEventLogger(private val dimensionId: Int, dimensionDir: File) : Runnable {
    private val queue = ConcurrentLinkedQueue<Event>()

    private val db: Db = Db(dimensionDir)
    private var nextId: Byte = 1

    private var shouldShutdown = false

    fun add(e: Event) {
        queue.add(e)
    }

    fun shutdown() {
        LOGGER.debug("[dim $dimensionId] marking shutdown flag")
        shouldShutdown = true
    }

    override fun run() {
        LOGGER.debug("[dim $dimensionId] logger started")

        while (!shouldShutdown) {
            val events = mutableListOf<Evt<BlockEvent>>()
            for (i in 0 until queue.size.coerceAtMost(15)) {
                val e = queue.poll() ?: break
                LOGGER.debug("[dim $dimensionId] writing event: $e")

                when (e) {
                    is BlockEvent -> {
                        events.add(Evt(nextId++, Instant.now(), e.pos.toLong(), ByteArray(0), e))
                        nextId = (nextId % Byte.MAX_VALUE).toByte()
                    }
                }
            }

            if (events.isNotEmpty()) {
                db.addEvents(events)
            }
        }

        LOGGER.debug("[dim $dimensionId] stopped")
    }

    private companion object {
        private val LOGGER = LogManager.getLogger(KryAudit.MOD_ID)
    }
}