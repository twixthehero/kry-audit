package com.krythera.audit.cmd

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands

class AuditCommand {
    companion object {
        fun register(): LiteralArgumentBuilder<CommandSource> {
            return Commands.literal("audit")
                .requires { it.hasPermissionLevel(3) }
                .then(QueryCommand.register())
        }
    }
}