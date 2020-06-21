package com.krythera.audit.init

import com.krythera.audit.cmd.AuditEventsArgument
import net.minecraft.command.arguments.ArgumentTypes

class KryArgumentTypes {
    companion object {
        /** Registers all kryaudit ArgumentTypes and their Serializers. */
        @JvmStatic
        fun register() {
            ArgumentTypes.register(
                "kryaudit.auditevents",
                AuditEventsArgument::class.java,
                AuditEventsArgument.Companion.Serializer()
            )
        }
    }
}