package cmd

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands

class KryCommand {
    companion object {
        @JvmStatic
        fun register(dispatcher: CommandDispatcher<CommandSource>) {
            dispatcher.register(
                Commands.literal("kry").then(
                    Commands.literal("audit").then(
                        Commands.literal("query").executes { 0 }
                    )
                )
            )
        }
    }
}