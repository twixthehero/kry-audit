package com.krythera.audit.cmd

import com.krythera.audit.blocks.ForgeBlockEvents
import com.krythera.audit.db.AuditEvent
import com.krythera.audit.flatbuffers.BlockBreakMetadata
import com.krythera.audit.flatbuffers.BlockPlaceMetadata
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.block.Block
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.command.arguments.BlockPosArgument
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ChatType
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.registries.IForgeRegistry
import org.apache.logging.log4j.LogManager
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date

class SingleCommand {
    companion object {
        private val LOGGER = LogManager.getLogger(SingleCommand::class.java)

        private val DIMENSION_UNLOADED =
            SimpleCommandExceptionType(TranslationTextComponent("dimension.unloaded"))

        private val DATE_FORMAT = SimpleDateFormat("MM-dd HH:mm:ss")

        private var _blockRegistry: IForgeRegistry<Block>? = null
        private val blockRegistry: IForgeRegistry<Block>
            get() {
                return _blockRegistry
                    ?: throw AssertionError("_blockRegistry was null during get()")
            }

        @ExperimentalUnsignedTypes
        fun register(): LiteralArgumentBuilder<CommandSource> =
            Commands.literal("single").then(
                Commands.argument("position", BlockPosArgument.blockPos())
                    .then(
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
                            })
            )

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
            val player = source.asPlayer()

            val results = logger.db.query(position, eventTypes)
            if (results.isEmpty()) {
                source.asPlayer()
                    .sendMessage(
                        TranslationTextComponent("kryaudit.noevents.single", position),
                        ChatType.CHAT
                    )
            } else {
                player.sendMessage(
                    TranslationTextComponent("(${position.x}, ${position.y}, ${position.z})"),
                    ChatType.CHAT
                )

                if (_blockRegistry == null) {
                    _blockRegistry = GameRegistry.findRegistry(Block::class.java)
                }

                var num = 1
                results.forEach {
                    val eventText: String
                    when (it.eventType) {
                        AuditEvent.PLACE -> {
                            val breakMetadata =
                                BlockBreakMetadata.getRootAsBlockBreakMetadata(
                                    ByteBuffer.wrap(it.metadata)
                                )

                            val locationParts =
                                breakMetadata.blockId?.split(":") ?: listOf(
                                    "minecraft",
                                    "air"
                                )
                            val block = blockRegistry.getValue(
                                ResourceLocation(locationParts[0], locationParts[1])
                            )
                            eventText =
                                "${DATE_FORMAT.format(Date.from(it.timestamp))} ${breakMetadata.playerName} placed ${block?.nameTextComponent?.unformattedComponentText}"
                        }
                        AuditEvent.BREAK -> {
                            val placeMetadata =
                                BlockPlaceMetadata.getRootAsBlockPlaceMetadata(
                                    ByteBuffer.wrap(it.metadata)
                                )

                            val locationParts =
                                placeMetadata.blockId?.split(":") ?: listOf(
                                    "minecraft",
                                    "air"
                                )
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

                    player.sendMessage(
                        StringTextComponent("${num++} | $eventText"),
                        ChatType.CHAT
                    )
                }
            }

            return Command.SINGLE_SUCCESS
        }
    }
}