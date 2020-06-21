package com.krythera.audit.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object TableItemEvents : IntIdTable() {
    val itemEventId = byte("itemEventId")
    val timestamp = timestamp("timestamp")

    override val primaryKey = PrimaryKey(itemEventId, timestamp)

    val eventType = customEnumeration(
        "eventType",
        "ENUM('${AuditEvent.AUDIT_EVENT_UNSPECIFIED}', '${AuditEvent.TOSS}', '${AuditEvent.EXPIRE}')",
        { AuditEvent.valueOf(it as String) },
        { it.name })
    val x = double("x")
    val y = double("y")
    val z = double("z")
    val itemId = varchar("itemId", 255).nullable()
    val throwerId = uuid("throwerId").nullable()
    val metadata = binary("metadata", 1024 * 1024 * 10)
}