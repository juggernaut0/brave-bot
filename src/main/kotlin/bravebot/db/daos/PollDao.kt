package bravebot.db.daos

import bravebot.db.Db
import bravebot.db.jooq.Tables.POLL
import bravebot.db.jooq.tables.records.PollRecord
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject

class PollDao @Inject constructor(db: Db) : Dao(db) {
    fun createPoll(name: String, serverId: Long, options: Array<String>, endDate: LocalDateTime): PollRecord {
        return dsl.newRecord(POLL).apply {
            this.name = name
            this.serverId = serverId
            this.setOptions(*options)
            this.endDt = Timestamp.valueOf(endDate)

            insert()
        }
    }

    fun getById(id: Int): PollRecord? {
        return dsl.selectFrom(POLL).where(POLL.ID.eq(id)).fetch().firstOrNull()
    }
}