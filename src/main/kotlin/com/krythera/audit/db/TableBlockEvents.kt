package com.krythera.audit.db

import com.krythera.audit.events.AuditEvent
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object TableBlockEvents : IntIdTable() {
    val blockEventId = byte("blockEventId")
    val timestamp = timestamp("timestamp")

    override val primaryKey = PrimaryKey(blockEventId, timestamp)

    val eventType = customEnumeration(
        "eventType",
        "ENUM('${AuditEvent.AUDIT_EVENT_UNSPECIFIED}', '${AuditEvent.BLOCK_BREAK}', '${AuditEvent.BLOCK_PLACE}')",
        { AuditEvent.valueOf(it as String) },
        { it.name })
    val x = integer("x")
    val y = integer("y")
    val z = integer("z")
    val metadata = binary("metadata", 1024 * 1024 * 10)
}