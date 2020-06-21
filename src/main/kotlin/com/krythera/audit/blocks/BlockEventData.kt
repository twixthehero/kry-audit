package com.krythera.audit.blocks

import com.krythera.audit.events.AuditEvent
import java.time.Instant

data class BlockEventData(
    val blockEventId: Byte,
    val timestamp: Instant,
    val eventType: AuditEvent,
    val x: Int,
    val y: Int,
    val z: Int,
    val metadata: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlockEventData

        if (blockEventId != other.blockEventId) return false
        if (timestamp != other.timestamp) return false
        if (eventType != other.eventType) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (!metadata.contentEquals(other.metadata)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0 + blockEventId
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + eventType.hashCode()
        result = 31 * result + x
        result = 31 * result + y
        result = 31 * result + z
        result = 31 * result + metadata.contentHashCode()
        return result
    }
}