package com.krythera.audit.db

import net.minecraftforge.eventbus.api.Event
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class Db(dimensionDir: File) {
    private val database = Database.connect("jdbc:h2:./$dimensionDir/data/kryaudit", driver = "org.h2.Driver")

    init {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(TableBlockEvents)
        }
    }

    fun <T : Event> addEvents(events: List<Evt<T>>) = transaction {
        events.forEach { e ->
            TableBlockEvents.insert {
                it[blockEventId] = e.id
                it[timestamp] = e.timestamp
                it[pos] = e.blockPos
                it[data] = e.metadata
            }
        }

        commit()
    }
}