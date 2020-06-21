package com.krythera.audit.cmd

import com.krythera.audit.blocks.BlockEventData
import com.krythera.audit.blocks.DefaultBlockAuditEvents
import com.krythera.audit.blocks.ForgeBlockEvents
import com.krythera.audit.db.AuditEvent
import com.krythera.audit.flatbuffers.BlockBreakMetadata
import com.krythera.audit.flatbuffers.BlockPlaceMetadata
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.command.arguments.BlockPosArgument
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ChatType
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import org.apache.logging.log4j.LogManager
import java.nio.ByteBuffer

class BoxCommand {
    companion object {
        private val LOGGER = LogManager.getLogger(BoxCommand::class.java)
        private val DIMENSION_UNLOADED =
            SimpleCommandExceptionType(TranslationTextComponent("dimension.unloaded"))

        @ExperimentalUnsignedTypes
        fun register(): LiteralArgumentBuilder<CommandSource> =
            Commands.literal("box").then(
                Commands.argument("startPos", BlockPosArgument.blockPos()).then(
                    Commands.argument("endPos", BlockPosArgument.blockPos()).then(
                        Commands.argument(
                            "eventTypes",
                            AuditEventsArgument.auditEvents()
                        )
                            .executes {
                                val dimensionId = it.source.world.dimension.type.id
                                val startPos =
                                    BlockPosArgument.getBlockPos(it, "startPos")
                                val endPos =
                                    BlockPosArgument.getBlockPos(it, "endPos")
                                val types =
                                    AuditEventsArgument.getAuditEvents(it, "eventTypes")

                                query(
                                    it.source,
                                    dimensionId,
                                    startPos,
                                    endPos,
                                    types
                                )
                            }
                    ).executes {
                        val dimensionId = it.source.world.dimension.type.id
                        val startPos =
                            BlockPosArgument.getBlockPos(it, "startPos")
                        val endPos =
                            BlockPosArgument.getBlockPos(it, "endPos")

                        query(
                            it.source,
                            dimensionId,
                            startPos,
                            endPos,
                            DefaultBlockAuditEvents.defaultEvents()
                        )
                    }
                )
            )

        @ExperimentalUnsignedTypes
        fun query(
            source: CommandSource,
            dimensionId: Int,
            startPos: BlockPos,
            endPos: BlockPos,
            eventTypes: Set<AuditEvent>
        ): Int {
            LOGGER.debug("dimension id: $dimensionId, start pos: $startPos, end pos: $endPos, eventTypes: $eventTypes")
            val logger = ForgeBlockEvents.getDimensionLogger(dimensionId)
                ?: throw DIMENSION_UNLOADED.create()

            val player = source.asPlayer()

            val results = logger.db.query(startPos, endPos, eventTypes)
            if (results.isEmpty()) {
                player.sendMessage(
                    TranslationTextComponent(
                        "kryaudit.noevents.multiple",
                        StringTextComponent("$startPos").applyTextStyle(TextFormatting.GREEN),
                        StringTextComponent("$startPos").applyTextStyle(TextFormatting.RED)
                    ),
                    ChatType.CHAT
                )
            } else {
                player.sendMessage(
                    StringTextComponent("${TextFormatting.GREEN}(${startPos.x}, ${startPos.y}, ${startPos.z})${TextFormatting.RESET} <=> ${TextFormatting.RED}(${endPos.x}, ${endPos.y}, ${endPos.z})"),
                    ChatType.CHAT
                )

                val events = mutableMapOf<AuditEvent, MutableList<BlockEventData>>()
                val playerCount = mutableMapOf<String?, Int>()
                var numEvents = 0
                AuditEvent.values().filterNot { it == AuditEvent.AUDIT_EVENT_UNSPECIFIED }
                    .forEach { it ->
                        val list = events.getOrPut(it) { mutableListOf() }
                        results.filter { e -> e.eventType == it }.forEach {
                            numEvents++
                            list.add(it)

                            when (it.eventType) {
                                AuditEvent.BREAK -> {
                                    val breakMetadata =
                                        BlockBreakMetadata.getRootAsBlockBreakMetadata(
                                            ByteBuffer.wrap(it.metadata)
                                        )

                                    playerCount[breakMetadata.playerName] =
                                        playerCount.getOrPut(breakMetadata.playerName) { 0 } + 1
                                }
                                AuditEvent.PLACE -> {
                                    val placeMetadata =
                                        BlockPlaceMetadata.getRootAsBlockPlaceMetadata(
                                            ByteBuffer.wrap(it.metadata)
                                        )

                                    playerCount[placeMetadata.entityName] =
                                        playerCount.getOrPut(placeMetadata.entityName) { 0 } + 1
                                }
                                else -> {
                                }
                            }
                        }
                    }

                player.sendMessage(
                    TranslationTextComponent(
                        "kryaudit.event.count",
                        numEvents
                    )
                )

                events.keys.filter { events[it]?.isNotEmpty() ?: false }.forEach {
                    player.sendMessage(StringTextComponent("${TextFormatting.RED}$it${TextFormatting.RESET} -> ${events[it]?.size}"))
                }

                player.sendMessage(
                    TranslationTextComponent(
                        "kryaudit.players",
                        playerCount.map { "${it.key} (${it.value})" }.joinToString(" ")
                    )
                )
            }

            return Command.SINGLE_SUCCESS
        }
    }
}