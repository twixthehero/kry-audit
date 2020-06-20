package com.krythera.audit.db

import org.jetbrains.exposed.dao.id.IntIdTable

object TableDbVersion : IntIdTable() {
    val version = integer("version")

    override val primaryKey = PrimaryKey(version)
}