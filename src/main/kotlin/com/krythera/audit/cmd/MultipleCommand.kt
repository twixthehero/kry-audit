package com.krythera.audit.cmd

import com.krythera.audit.blocks.DefaultBlockAuditEvents
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent

class MultipleCommand {
    companion object {
        @ExperimentalUnsignedTypes
        fun register(): LiteralArgumentBuilder<CommandSource> =
            Commands.literal("multiple")
                .then(BoxCommand.register())
                .then(RadiusCommand.register())
                .then(Commands.argument("help", StringArgumentType.word()).executes {
                    showHelp(it.source)
                })
                .executes {
                    showHelp(it.source)
                }

        private fun showHelp(source: CommandSource): Int {
            source.asPlayer().apply {
                sendMessage(TranslationTextComponent("kryaudit.command.kry.audit.query.multiple.title"))
                sendMessage(StringTextComponent(""))
                sendMessage(
                    TranslationTextComponent(
                        "kryaudit.command.kry.audit.query.multiple.box",
                        StringTextComponent("eventTypes").applyTextStyle(TextFormatting.YELLOW)
                    )
                )
                sendMessage(
                    TranslationTextComponent(
                        "kryaudit.command.kry.audit.query.multiple.radius.xyz",
                        StringTextComponent("x").applyTextStyle(TextFormatting.RED),
                        StringTextComponent("${position.x}"),
                        StringTextComponent("y").applyTextStyle(TextFormatting.GREEN),
                        StringTextComponent("${position.y}"),
                        StringTextComponent("z").applyTextStyle(TextFormatting.BLUE),
                        StringTextComponent("${position.z}"),
                        StringTextComponent("eventTypes").applyTextStyle(TextFormatting.YELLOW),
                        StringTextComponent("x").applyTextStyle(TextFormatting.RED),
                        StringTextComponent("y").applyTextStyle(TextFormatting.GREEN),
                        StringTextComponent("z").applyTextStyle(TextFormatting.BLUE)
                    )
                )
                sendMessage(
                    TranslationTextComponent(
                        "kryaudit.command.kry.audit.query.multiple.radius.size",
                        TranslationTextComponent("kryaudit.command.size").applyTextStyle(
                            TextFormatting.BLUE
                        ),
                        RadiusCommand.DEFAULT_SIZE,
                        StringTextComponent("eventTypes").applyTextStyle(TextFormatting.YELLOW),
                        TranslationTextComponent("kryaudit.command.size").applyTextStyle(
                            TextFormatting.BLUE
                        )
                    )
                )
                sendMessage(StringTextComponent(""))
                sendMessage(
                    TranslationTextComponent(
                        "kryaudit.command.kry.audit.query.eventtypes",
                        StringTextComponent("eventTypes").applyTextStyle(TextFormatting.YELLOW),
                        StringTextComponent(
                            DefaultBlockAuditEvents.defaultEvents().joinToString(", ")
                        )
                    )
                )
                sendMessage(StringTextComponent(""))
                sendMessage(TranslationTextComponent("kryaudit.command.kry.help"))
            }

            return Command.SINGLE_SUCCESS
        }
    }
}