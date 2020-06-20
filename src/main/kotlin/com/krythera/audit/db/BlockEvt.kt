package com.krythera.audit.db

import com.krythera.audit.events.AuditEvent
import java.time.Instant

class BlockEvt(
    val blockEventId: Byte,
    val timestamp: Instant,
    val eventType: AuditEvent,
    val x: Int,
    val y: Int,
    val z: Int,
    val metadata: ByteArray
)