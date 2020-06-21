package com.krythera.audit.cmd

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands

class QueryCommand {
    companion object {
        @ExperimentalUnsignedTypes
        fun register(): LiteralArgumentBuilder<CommandSource> =
            Commands.literal("query").then(SingleCommand.register())
                .then(MultipleCommand.register())
    }
}