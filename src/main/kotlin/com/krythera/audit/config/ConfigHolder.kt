package com.krythera.audit.config

import net.minecraftforge.common.ForgeConfigSpec

class ConfigHolder {
    companion object {
        var SERVER_SPEC: ForgeConfigSpec
        var SERVER: ServerConfig

        init {
            val specPair = ForgeConfigSpec.Builder().configure {
                ServerConfig(
                    it
                )
            }
            SERVER = specPair.left
            SERVER_SPEC = specPair.right
        }
    }
}