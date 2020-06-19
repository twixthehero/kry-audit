import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent
import org.apache.logging.log4j.LogManager

@Mod(KryAuditKt.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = KryAuditKt.MOD_ID)
class KryAuditKt {
    companion object {
        const val MOD_ID = "kryaudit"

        private val LOGGER = LogManager.getLogger(MOD_ID)

        @SubscribeEvent
        @JvmStatic
        fun commonSetup(e: FMLCommonSetupEvent) {
            LOGGER.debug("common setup")
            val ctx = ModLoadingContext.get()
            ctx.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC)
        }

        @SubscribeEvent
        @JvmStatic
        fun clientSetup(e: FMLClientSetupEvent) {
            LOGGER.debug("client setup")
        }

        @SubscribeEvent
        @JvmStatic
        fun dedicatedServerSetup(e: FMLDedicatedServerSetupEvent) {
            LOGGER.debug("dedicated server setup")
        }

        @SubscribeEvent
        @JvmStatic
        fun enqueueImc(e: InterModEnqueueEvent) {
            LOGGER.debug("enqueue imc")
        }

        @SubscribeEvent
        @JvmStatic
        fun processImc(e: InterModProcessEvent) {
            LOGGER.debug("process imc")
        }
    }
}
