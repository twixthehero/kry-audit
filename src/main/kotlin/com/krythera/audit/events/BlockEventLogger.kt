package com.krythera.audit.events

import com.google.flatbuffers.FlatBufferBuilder
import com.krythera.audit.KryAudit
import com.krythera.audit.db.BlockEvt
import com.krythera.audit.db.Db
import com.krythera.audit.flatbuffers.BlockBreakMetadata
import com.krythera.audit.flatbuffers.BlockPlaceMetadata
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

    @ExperimentalUnsignedTypes
    override fun run() {
        LOGGER.debug("[dim $dimensionId] logger started")

        while (!shouldShutdown) {
            val events = mutableListOf<BlockEvt>()
            for (i in 0 until queue.size.coerceAtMost(15)) {
                val e = queue.poll() ?: break
                LOGGER.debug("[dim $dimensionId] adding event: $e")

                when (e) {
                    is BlockEvent.BreakEvent -> {
                        LOGGER.debug("break pos: ${e.pos}")
                        val builder = FlatBufferBuilder()

                        val playerUuid = builder.createString(e.player.uniqueID.toString())
                        val playerName =
                            builder.createString(e.player.name.unformattedComponentText)

                        val blockRegistryName = e.state.block.registryName?.toString()
                        if (blockRegistryName == null) {
                            LOGGER.warn("null block registry name @ ${e.pos} - ${e.state.block.nameTextComponent.unformattedComponentText}")
                        }

                        val blockId = builder.createString(blockRegistryName)

                        builder.finish(
                            BlockBreakMetadata.createBlockBreakMetadata(
                                builder,
                                playerUuid,
                                playerName,
                                blockId
                            )
                        )

                        events.add(
                            BlockEvt(
                                getNextId(),
                                Instant.now(),
                                AuditEvent.BLOCK_BREAK,
                                e.pos.toLong(),
                                builder.sizedByteArray()
                            )
                        )
                    }
                    is BlockEvent.EntityPlaceEvent -> {
                        LOGGER.debug("place pos: ${e.pos}")
                        val builder = FlatBufferBuilder()

                        val entityUuid = builder.createString(e.entity?.uniqueID.toString())
                        val entityName =
                            builder.createString(e.entity?.name?.unformattedComponentText)

                        val newBlock = e.blockSnapshot.currentBlock.block
                        val blockRegistryName = newBlock.registryName?.toString()
                        if (blockRegistryName == null) {
                            LOGGER.warn("null block registry name @ ${e.pos} - ${newBlock.nameTextComponent.unformattedComponentText}")
                        }

                        val blockId = builder.createString(blockRegistryName)

                        builder.finish(
                            BlockPlaceMetadata.createBlockPlaceMetadata(
                                builder,
                                entityUuid,
                                entityName,
                                blockId
                            )
                        )

                        //TODO: store the blockstate in db for reverting
//                        val blockProperties = e.blockSnapshot.currentBlock.values
//                        blockProperties.forEach { k, v ->
//                        }

                        events.add(
                            BlockEvt(
                                getNextId(),
                                Instant.now(),
                                AuditEvent.BLOCK_PLACE,
                                e.pos.toLong(),
                                builder.sizedByteArray()
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
        private val LOGGER = LogManager.getLogger(KryAudit.MOD_ID)
    }
}