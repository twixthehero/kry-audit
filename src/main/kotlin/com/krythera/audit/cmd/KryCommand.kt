package com.krythera.audit.cmd

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource

class KryCommand {
    companion object {
        @ExperimentalUnsignedTypes
        @JvmStatic
        fun register(dispatcher: CommandDispatcher<CommandSource>) {
            dispatcher.register(
                LiteralArgumentBuilder.literal<CommandSource>("kry")
                    .then(AuditCommand.register())
            )
        }
    }
}