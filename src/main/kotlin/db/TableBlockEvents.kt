package db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object TableBlockEvents : IntIdTable() {
    val blockEventId = byte("blockEventId")
    val timestamp = timestamp("timestamp")

    override val primaryKey = PrimaryKey(blockEventId, timestamp)

    val pos = long("pos")

    val data = binary("metadata", 1024 * 1024 * 10)
}