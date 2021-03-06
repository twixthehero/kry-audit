package com.krythera.audit

import com.krythera.audit.config.ConfigHolder
import com.krythera.audit.init.KryArgumentTypes
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
import net.minecraftforge.fml.network.FMLNetworkConstants
import org.apache.logging.log4j.LogManager
import java.util.function.BiPredicate
import java.util.function.Supplier

/** Mod file for kryaudit. */
@Mod(KryAudit.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = KryAudit.MOD_ID)
class KryAudit {
    init {
        // configure mod as server-only
        ModLoadingContext.get().registerExtensionPoint(
            ExtensionPoint.DISPLAYTEST
        ) {
            org.apache.commons.lang3.tuple.Pair.of(
                Supplier { FMLNetworkConstants.IGNORESERVERONLY },
                BiPredicate<String, Boolean> { _, _ -> true }
            )
        }
    }

    companion object {
        const val MOD_ID = "kryaudit"

        private val LOGGER = LogManager.getLogger(KryAudit::class.java)

        /** 1: Handles Forge common setup */
        @SubscribeEvent
        @JvmStatic
        fun commonSetup(e: FMLCommonSetupEvent) {
            LOGGER.debug("common setup")
            DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER) {
                DistExecutor.SafeRunnable {
                    val ctx = ModLoadingContext.get()
                    ctx.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC)
                }
            }

            KryArgumentTypes.register()
        }

        /** 2: Handles Forge client sided setup */
        @SubscribeEvent
        @JvmStatic
        fun clientSetup(e: FMLClientSetupEvent) {
            LOGGER.debug("client setup")
        }

        /** 2: Handles Forge server sided setup */
        @SubscribeEvent
        @JvmStatic
        fun dedicatedServerSetup(e: FMLDedicatedServerSetupEvent) {
            LOGGER.debug("dedicated server setup")
        }

        /** 3: Handles Forge enqueue IMC */
        @SubscribeEvent
        @JvmStatic
        fun enqueueImc(e: InterModEnqueueEvent) {
            LOGGER.debug("enqueue imc")
        }

        /** 4: Handles Forge process IMC */
        @SubscribeEvent
        @JvmStatic
        fun processImc(e: InterModProcessEvent) {
            LOGGER.debug("process imc")
        }
    }
}
