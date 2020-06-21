package com.krythera.audit.items

import com.krythera.audit.KryAudit
import com.krythera.audit.db.DatabaseConnectionManager
import net.minecraftforge.event.entity.item.ItemExpireEvent
import net.minecraftforge.event.entity.item.ItemTossEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors

@Mod.EventBusSubscriber(
    bus = Mod.EventBusSubscriber.Bus.FORGE,
    modid = KryAudit.MOD_ID
)
class ForgeItemEvents {
    companion object {
        private val LOGGER = LogManager.getLogger(ForgeItemEvents::class.java)

        // one thread per loaded dimension for logging
        private val executor = Executors.newCachedThreadPool()
        private val dimensionLoggers = mutableMapOf<Int, ItemEventLogger>()

        fun getDimensionLogger(dimensionId: Int): ItemEventLogger? {
            return dimensionLoggers[dimensionId]
        }

        @SubscribeEvent
        @JvmStatic
        fun onLoadWorld(e: WorldEvent.Load) {
            if (e.world.isRemote) {
                return
            }

            val dimId = e.world.dimension.type.id
            LOGGER.debug("loading world dimension $dimId")

            dimensionLoggers.computeIfAbsent(dimId) {
                val logger =
                    ItemEventLogger(dimId, DatabaseConnectionManager.getDatabase(dimId))

                executor.submit(logger)

                logger
            }
        }

        @SubscribeEvent
        @JvmStatic
        fun onUnloadWorld(e: WorldEvent.Unload) {
            if (e.world.isRemote) {
                return
            }

            val dimId = e.world.dimension.type.id
            LOGGER.debug("unloading world dimension $dimId")

            val eventLogger = dimensionLoggers.remove(dimId)
            eventLogger?.shutdown()
        }

        @SubscribeEvent
        @JvmStatic
        fun onItemToss(e: ItemTossEvent) {
            LOGGER.debug("toss: ${e.player.displayNameAndUUID} -> ${e.entity}")
            dimensionLoggers[e.player.dimension.id]?.add(e)
        }

        @SubscribeEvent
        @JvmStatic
        fun onItemExpire(e: ItemExpireEvent) {
            LOGGER.debug("expire: ${e.entity}")
            dimensionLoggers[e.entity.dimension.id]?.add(e)
        }
    }
}