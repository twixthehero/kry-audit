package com.krythera.audit.items

import com.krythera.audit.db.AuditEvent
import java.time.Instant
import java.util.UUID

data class ItemEventData(
    val itemEventId: Byte,
    val timestamp: Instant,
    val eventType: AuditEvent,
    val x: Double,
    val y: Double,
    val z: Double,
    val itemId: String?,
    val throwerId: UUID?,
    val metadata: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemEventData

        if (itemEventId != other.itemEventId) return false
        if (timestamp != other.timestamp) return false
        if (eventType != other.eventType) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (itemId != other.itemId) return false
        if (throwerId != other.throwerId) return false
        if (!metadata.contentEquals(other.metadata)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0 + itemEventId
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + eventType.hashCode()
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        result = 31 * result + (itemId?.hashCode() ?: 0)
        result = 31 * result + throwerId.hashCode()
        result = 31 * result + metadata.contentHashCode()
        return result
    }
}