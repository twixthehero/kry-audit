package com.krythera.audit.cmd

import com.krythera.audit.events.AuditEvent
import com.krythera.audit.events.ForgeBlockEvents
import com.krythera.audit.flatbuffers.BlockBreakMetadata
import com.krythera.audit.flatbuffers.BlockPlaceMetadata
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.block.Block
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.command.arguments.BlockPosArgument
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ChatType
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.common.registry.GameRegistry
import org.apache.logging.log4j.LogManager
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date

class KryCommand {
    companion object {
        private val LOGGER = LogManager.getLogger(KryCommand::class.java)

        private val DIMENSION_UNLOADED =
            SimpleCommandExceptionType(TranslationTextComponent("dimension.unloaded"))

        private val DATE_FORMAT = SimpleDateFormat("MM-dd HH:mm:ss")

        @ExperimentalUnsignedTypes
        @JvmStatic
        fun register(dispatcher: CommandDispatcher<CommandSource>) {
            dispatcher.register(
                Commands.literal("kry").then(
                    Commands.literal("audit")
                        .requires { it.hasPermissionLevel(3) }
                        .then(
                            Commands.literal("query").then(
                                Commands.argument("position", BlockPosArgument.blockPos()).then(
                                    Commands.argument(
                                        "eventTypes",
                                        AuditEventsArgument.auditEvents()
                                    )
                                        .executes {
                                            val dimensionId = it.source.world.dimension.type.id
                                            val position =
                                                BlockPosArgument.getBlockPos(it, "position")
                                            val types =
                                                AuditEventsArgument.getAuditEvents(it, "eventTypes")

                                            query(it.source, dimensionId, position, types)
                                        }
                                )
                            )
                        )
                )
            )
        }

        @ExperimentalUnsignedTypes
        private fun query(
            source: CommandSource,
            dimensionId: Int,
            position: BlockPos,
            eventTypes: Set<AuditEvent>
        ): Int {
            LOGGER.debug("dimension id: $dimensionId, pos: $position, eventTypes: $eventTypes")
            val logger = ForgeBlockEvents.getDimensionLogger(dimensionId)
                ?: throw DIMENSION_UNLOADED.create()

            val results = logger.db.query(position.toLong(), eventTypes)
            if (results.isEmpty()) {
                source.asPlayer()
                    .sendMessage(
                        TranslationTextComponent("kryaudit.noevents", position),
                        ChatType.CHAT
                    )
            } else {
                source.asPlayer()
                    .sendMessage(
                        TranslationTextComponent("(${position.x}, ${position.y}, ${position.z})"),
                        ChatType.CHAT
                    )

                var num = 1
                results.forEach {
                    val blockRegistry = GameRegistry.findRegistry(Block::class.java)

                    val eventText: String
                    when (it.eventType) {
                        AuditEvent.BLOCK_PLACE -> {
                            val breakMetadata = BlockBreakMetadata.getRootAsBlockBreakMetadata(
                                ByteBuffer.wrap(it.metadata)
                            )

                            val locationParts =
                                breakMetadata.blockId?.split(":") ?: listOf("minecraft", "air")
                            val block = blockRegistry.getValue(
                                ResourceLocation(locationParts[0], locationParts[1])
                            )
                            eventText =
                                "${DATE_FORMAT.format(Date.from(it.timestamp))} ${breakMetadata.playerName} placed ${block?.nameTextComponent?.unformattedComponentText}"
                        }
                        AuditEvent.BLOCK_BREAK -> {
                            val placeMetadata = BlockPlaceMetadata.getRootAsBlockPlaceMetadata(
                                ByteBuffer.wrap(it.metadata)
                            )

                            val locationParts =
                                placeMetadata.blockId?.split(":") ?: listOf("minecraft", "air")
                            val block = blockRegistry.getValue(
                                ResourceLocation(locationParts[0], locationParts[1])
                            )
                            eventText =
                                "${DATE_FORMAT.format(Date.from(it.timestamp))} ${placeMetadata.entityName} broke ${block?.nameTextComponent?.unformattedComponentText}"
                        }
                        else -> {
                            eventText =
                                "${DATE_FORMAT.format(Date.from(it.timestamp))} Unknown event type '${it.eventType}'"
                        }
                    }

                    source.asPlayer()
                        .sendMessage(
                            TranslationTextComponent("${num++} | $eventText"),
                            ChatType.CHAT
                        )
                }
            }

            return Command.SINGLE_SUCCESS
        }
    }
}