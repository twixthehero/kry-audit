package com.krythera.audit.blocks

import com.krythera.audit.KryAudit
import com.krythera.audit.db.DatabaseConnectionManager
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors

@Mod.EventBusSubscriber(
    bus = Mod.EventBusSubscriber.Bus.FORGE,
    modid = KryAudit.MOD_ID
)
class ForgeBlockEvents {
    companion object {
        private val LOGGER = LogManager.getLogger(ForgeBlockEvents::class.java)

        // one thread per loaded dimension for logging
        private val executor = Executors.newCachedThreadPool()
        private val dimensionLoggers = mutableMapOf<Int, BlockEventLogger>()

        fun getDimensionLogger(dimensionId: Int): BlockEventLogger? {
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
                    BlockEventLogger(dimId, DatabaseConnectionManager.getDatabase(dimId))

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
        fun onBlockBreak(e: BlockEvent.BreakEvent) {
            LOGGER.debug("break: ${e.player.displayNameAndUUID} @ ${e.pos} (${e.state.block})")
            val dimId = e.world.dimension.type.id
            val logger = dimensionLoggers[dimId]
            logger?.add(e)
        }

        @SubscribeEvent
        @JvmStatic
        fun onBlockPlace(e: BlockEvent.EntityPlaceEvent) {
            LOGGER.debug("place: ${e.entity} @ ${e.pos} (${e.state.block})")
            val dimId = e.world.dimension.type.id
            val logger = dimensionLoggers[dimId]
            logger?.add(e)
        }
    }
}