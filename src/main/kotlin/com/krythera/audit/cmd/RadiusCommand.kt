package com.krythera.audit.cmd

import com.krythera.audit.db.AuditEvent
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.util.math.BlockPos
import kotlin.math.abs

class RadiusCommand {
    companion object {
        // TODO: replace with config value
        private const val DEFAULT_SIZE = 10

        @ExperimentalUnsignedTypes
        fun register(): LiteralArgumentBuilder<CommandSource> =
            Commands.literal("radius")
                .then(
                    Commands.argument("x", IntegerArgumentType.integer(1)).then(
                        Commands.argument("y", IntegerArgumentType.integer(1)).then(
                            Commands.argument("z", IntegerArgumentType.integer(1))
                                .then(Commands.argument(
                                    "eventTypes",
                                    AuditEventsArgument.auditEvents()
                                )
                                    .executes {
                                        val x = abs(IntegerArgumentType.getInteger(it, "x"))
                                        val y = abs(IntegerArgumentType.getInteger(it, "y"))
                                        val z = abs(IntegerArgumentType.getInteger(it, "z"))
                                        val eventTypes =
                                            AuditEventsArgument.getAuditEvents(it, "eventTypes")
                                        val position = it.source.pos

                                        query(
                                            it.source,
                                            BlockPos(
                                                position.x - x,
                                                position.y - y,
                                                position.z - z
                                            ),
                                            BlockPos(
                                                position.x + x,
                                                position.y + y,
                                                position.z + z
                                            ),
                                            eventTypes
                                        )
                                    }
                                ).executes {
                                    val x = abs(IntegerArgumentType.getInteger(it, "x"))
                                    val y = abs(IntegerArgumentType.getInteger(it, "y"))
                                    val z = abs(IntegerArgumentType.getInteger(it, "z"))
                                    val position = it.source.pos

                                    query(
                                        it.source,
                                        BlockPos(
                                            position.x - x,
                                            position.y - y,
                                            position.z - z
                                        ),
                                        BlockPos(
                                            position.x + x,
                                            position.y + y,
                                            position.z + z
                                        ),
                                        defaultEventTypes()
                                    )
                                }
                        )
                    )
                ).then(
                    Commands.argument("size", IntegerArgumentType.integer(1))
                        .then(
                            Commands.argument("eventTypes", AuditEventsArgument.auditEvents())
                                .executes {
                                    val size = abs(IntegerArgumentType.getInteger(it, "size"))
                                    val eventTypes =
                                        AuditEventsArgument.getAuditEvents(it, "eventTypes")
                                    val position = it.source.pos

                                    query(
                                        it.source,
                                        BlockPos(
                                            position.x - size,
                                            position.y - size,
                                            position.z - size
                                        ),
                                        BlockPos(
                                            position.x + size,
                                            position.y + size,
                                            position.z + size
                                        ),
                                        eventTypes
                                    )
                                }
                        ).executes {
                            val size = abs(IntegerArgumentType.getInteger(it, "size"))
                            val position = it.source.pos

                            query(
                                it.source,
                                BlockPos(
                                    position.x - size,
                                    position.y - size,
                                    position.z - size
                                ),
                                BlockPos(
                                    position.x + size,
                                    position.y + size,
                                    position.z + size
                                ),
                                defaultEventTypes()
                            )
                        }
                ).then(
                    Commands.argument("eventTypes", AuditEventsArgument.auditEvents()).executes {
                        val eventTypes =
                            AuditEventsArgument.getAuditEvents(it, "eventTypes")
                        val position = it.source.pos

                        query(
                            it.source,
                            BlockPos(
                                position.x - DEFAULT_SIZE,
                                position.y - DEFAULT_SIZE,
                                position.z - DEFAULT_SIZE
                            ),
                            BlockPos(
                                position.x + DEFAULT_SIZE,
                                position.y + DEFAULT_SIZE,
                                position.z + DEFAULT_SIZE
                            ),
                            eventTypes
                        )
                    }
                ).executes {
                    val position = it.source.pos
                    query(
                        it.source,
                        BlockPos(
                            position.x - DEFAULT_SIZE,
                            position.y - DEFAULT_SIZE,
                            position.z - DEFAULT_SIZE
                        ),
                        BlockPos(
                            position.x + DEFAULT_SIZE,
                            position.y + DEFAULT_SIZE,
                            position.z + DEFAULT_SIZE
                        ),
                        defaultEventTypes()
                    )
                }

        @ExperimentalUnsignedTypes
        private fun query(
            source: CommandSource,
            startPos: BlockPos,
            endPos: BlockPos,
            eventTypes: Set<AuditEvent>
        ): Int =
            BoxCommand.query(source, source.world.dimension.type.id, startPos, endPos, eventTypes)

        private fun defaultEventTypes(): Set<AuditEvent> = setOf(AuditEvent.BREAK, AuditEvent.PLACE)
    }
}