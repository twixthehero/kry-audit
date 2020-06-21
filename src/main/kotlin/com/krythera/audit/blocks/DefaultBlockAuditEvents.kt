package com.krythera.audit.blocks

import com.krythera.audit.db.AuditEvent

object DefaultBlockAuditEvents {
    fun defaultEvents() = setOf(AuditEvent.BREAK, AuditEvent.PLACE)
}