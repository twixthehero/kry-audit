package com.krythera.audit.events

import com.krythera.audit.KryAudit
import net.minecraft.world.server.ServerMultiWorld
import net.minecraft.world.server.ServerWorld
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
        private val LOGGER = LogManager.getLogger(KryAudit.MOD_ID)

        private val executor = Executors.newFixedThreadPool(3)
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
            val world = when (e.world) {
                is ServerMultiWorld -> {
                    e.world as ServerMultiWorld
                }
                is ServerWorld -> {
                    e.world as ServerWorld
                }
                else -> throw RuntimeException("invalid world type: ${e.world}")
            }
            val dir = e.world.dimension.type.getDirectory(world.saveHandler.worldDirectory)
            LOGGER.debug("loading world dimension: $dimId ($dir)")

            dimensionLoggers.computeIfAbsent(dimId) {
                val logger =
                    BlockEventLogger(dimId, dir)

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
            LOGGER.debug("unloading world dimension: $dimId")

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