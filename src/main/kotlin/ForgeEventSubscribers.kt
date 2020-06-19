import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent
import net.minecraftforge.fml.event.server.FMLServerStartedEvent
import net.minecraftforge.fml.event.server.FMLServerStartingEvent
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent
import org.apache.logging.log4j.LogManager

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = KryAuditKt.MOD_ID)
class ForgeEventSubscribers {
    companion object {
        private val LOGGER = LogManager.getLogger(KryAuditKt.MOD_ID)

        // both sides' physical server events
        @SubscribeEvent
        @JvmStatic
        fun aboutToStart(e: FMLServerAboutToStartEvent) {
            LOGGER.debug("server about to start")
        }

        @SubscribeEvent
        @JvmStatic
        fun starting(e: FMLServerStartingEvent) {
            LOGGER.debug("server starting")
            // TODO: Register commands
        }

        @SubscribeEvent
        @JvmStatic
        fun started(e: FMLServerStartedEvent) {
            LOGGER.debug("server started")
        }

        @SubscribeEvent
        @JvmStatic
        fun stopping(e: FMLServerStoppingEvent) {
            LOGGER.debug("server stopping")
        }

        @SubscribeEvent
        @JvmStatic
        fun stopped(e: FMLServerStoppedEvent) {
            LOGGER.debug("server stopped")
        }
    }
}