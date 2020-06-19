package db

import net.minecraftforge.eventbus.api.Event
import java.time.Instant

class Evt<T : Event>(
    val id: Byte,
    val timestamp: Instant,
    val blockPos: Long,
    val metadata: ByteArray,
    val event: T
)