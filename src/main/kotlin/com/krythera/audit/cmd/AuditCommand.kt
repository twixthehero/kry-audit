package com.krythera.audit.cmd

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent

class AuditCommand {
    companion object {
        @ExperimentalUnsignedTypes
        fun register(): LiteralArgumentBuilder<CommandSource> {
            return Commands.literal("audit")
                .requires { it.hasPermissionLevel(3) }
                .then(QueryCommand.register())
                .then(Commands.argument("help", StringArgumentType.word()).executes {
                    showHelp(it.source)
                })
                .executes {
                    showHelp(it.source)
                }
        }

        private fun showHelp(source: CommandSource): Int {
            source.asPlayer().apply {
                sendMessage(TranslationTextComponent("kryaudit.command.kry.audit.title"))
                sendMessage(StringTextComponent(""))
                sendMessage(TranslationTextComponent("kryaudit.command.kry.audit.query"))
                sendMessage(StringTextComponent(""))
                sendMessage(TranslationTextComponent("kryaudit.command.kry.help"))
            }

            return Command.SINGLE_SUCCESS
        }
    }
}