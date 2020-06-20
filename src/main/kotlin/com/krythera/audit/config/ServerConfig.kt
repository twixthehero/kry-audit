package com.krythera.audit.config

import net.minecraftforge.common.ForgeConfigSpec

class ServerConfig(builder: ForgeConfigSpec.Builder) {
//    private lateinit var x: ForgeConfigSpec.BooleanValue

    /**
     * number of events to batch when writing to db
     *
     * default search AuditEvents?
     */

    init {
        builder.push("general")
        //TODO: add list of dimension IDs to log
        builder.pop()
    }
}