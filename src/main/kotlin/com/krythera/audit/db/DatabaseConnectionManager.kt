package com.krythera.audit.db

import com.krythera.audit.KryAudit
import net.minecraft.world.server.ServerMultiWorld
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/** Handles loading and unloading [Database] connections. */
@Mod.EventBusSubscriber(
    bus = Mod.EventBusSubscriber.Bus.FORGE,
    modid = KryAudit.MOD_ID
)
class DatabaseConnectionManager {
    companion object {
        private const val VERSION = 3
        private const val DRIVER = "org.h2.Driver"

        private val LOGGER = LogManager.getLogger(DatabaseConnectionManager::class.java)

        private val connections = mutableMapOf<Int, Database>()
        private val baseFolderPaths = mutableMapOf<Int, String>()

        /** Initiates a Db connection when a dimension is loaded. */
        @SubscribeEvent(priority = EventPriority.HIGH)
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
            loadDb(dimId, dir)
        }

        private fun loadDb(dimensionId: Int, dimensionDir: File) {
            LOGGER.debug("loading db for dimension $dimensionId: $dimensionDir")
            if (isDbLoaded(dimensionId)) {
                LOGGER.debug("dimension $dimensionId is already loaded!")
                return
            }

            baseFolderPaths[dimensionId] = dimensionDir.toString()

            val dbFile = "${baseFolderPaths[dimensionId]?.replace('\\', '/')}/data/kryaudit"
            var database = Database.connect("jdbc:h2:$dbFile", DRIVER)

            transaction(database) {
                // ensure the db version table exists before trying to query it.
                // it will not exist yet on new mod installs
                SchemaUtils.createMissingTablesAndColumns(TableDbVersion)
            }

            // if db has been updated, need to roll to another db file
            val currentVersion = getDbVersion(database)
            if (VERSION != currentVersion) {
                LOGGER.info("old database version found, rolling to new version file: $currentVersion -> $VERSION")

                // delete old trace file
                Files.deleteIfExists(Paths.get("$dbFile.trace.db"))

                // move previous db
                val oldVersionFile = "$dbFile.mv.db.v$currentVersion"
                Files.move(Paths.get("$dbFile.mv.db"), Paths.get(oldVersionFile))

                // recreate db
                database = Database.connect("jdbc:h2:$dbFile", DRIVER)

                // set version in new db
                transaction(database) {
                    SchemaUtils.createMissingTablesAndColumns(TableDbVersion)
                    SchemaUtils.createMissingTablesAndColumns(TableBlockEvents)

                    TableDbVersion.insert {
                        it[version] = VERSION
                    }

                    commit()
                }
            }

            connections[dimensionId] = database
            LOGGER.debug("dimension $dimensionId loaded")
        }

        private fun isDbLoaded(dimensionId: Int) = connections.containsKey(dimensionId)

        private fun getDbVersion(database: Database): Int {
            var version: Int? = null

            transaction(database) {
                version = TableDbVersion.selectAll().firstOrNull()?.get(TableDbVersion.version)
            }

            return version ?: 0
        }

        @SubscribeEvent
        @JvmStatic
        fun onUnloadWorld(e: WorldEvent.Unload) {
            if (e.world.isRemote) {
                return
            }

            val dimId = e.world.dimension.type.id
            LOGGER.debug("unloading world dimension: $dimId")

            // only transactions cause the connection to open, so it is safe to remove
            connections.remove(dimId)
        }

        /** Returns the [Database] for [dimensionId]. */
        fun getDatabase(dimensionId: Int): Database {
            if (!connections.containsKey(dimensionId)) {
                LOGGER.warn("attempt to retrieve db for dimension $dimensionId when it was not loaded")

                if (!baseFolderPaths.containsKey(dimensionId)) {
                    throw RuntimeException("tried to retrieve db for dimension $dimensionId which was never previously loaded")
                }

                loadDb(
                    dimensionId,
                    File(
                        baseFolderPaths[dimensionId]
                            ?: throw RuntimeException("base folder path for dimension $dimensionId was null after check")
                    )
                )
            }

            return connections[dimensionId]
                ?: throw RuntimeException("db for dimension $dimensionId was null")
        }
    }
}