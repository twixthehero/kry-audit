package com.krythera.audit.cmd

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent

class KryCommand {
    companion object {
        @ExperimentalUnsignedTypes
        @JvmStatic
        fun register(dispatcher: CommandDispatcher<CommandSource>) {
            dispatcher.register(
                LiteralArgumentBuilder.literal<CommandSource>("kry")
                    .then(AuditCommand.register())
                    .then(Commands.argument("help", StringArgumentType.word()).executes {
                        showHelp(it.source)
                    })
                    .executes {
                        showHelp(it.source)
                    }
            )
        }

        private fun showHelp(source: CommandSource): Int {
            source.asPlayer().apply {
                sendMessage(TranslationTextComponent("kryaudit.command.kry.title"))
                sendMessage(StringTextComponent(""))
                sendMessage(TranslationTextComponent("kryaudit.command.kry.audit"))
                sendMessage(StringTextComponent(""))
                sendMessage(TranslationTextComponent("kryaudit.command.kry.help"))
            }

            return Command.SINGLE_SUCCESS
        }
    }
}
