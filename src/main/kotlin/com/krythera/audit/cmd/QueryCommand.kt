package com.krythera.audit.cmd

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent

class QueryCommand {
    companion object {
        @ExperimentalUnsignedTypes
        fun register(): LiteralArgumentBuilder<CommandSource> =
            Commands.literal("query")
                .then(SingleCommand.register())
                .then(MultipleCommand.register())
                .then(Commands.argument("help", StringArgumentType.word()).executes {
                    showHelp(it.source)
                })
                .executes {
                    showHelp(it.source)
                }

        private fun showHelp(source: CommandSource): Int {
            source.asPlayer().apply {
                sendMessage(TranslationTextComponent("kryaudit.command.kry.audit.query.title"))
                sendMessage(StringTextComponent(""))
                sendMessage(TranslationTextComponent("kryaudit.command.kry.audit.query.single"))
                sendMessage(TranslationTextComponent("kryaudit.command.kry.audit.query.multiple"))
                sendMessage(StringTextComponent(""))
                sendMessage(TranslationTextComponent("kryaudit.command.kry.help"))
            }

            return Command.SINGLE_SUCCESS
        }
    }
}