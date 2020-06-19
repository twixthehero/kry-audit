package com.krythera.audit.db

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

class BlockEvent(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, BlockEvent>(IntIdTable())
}