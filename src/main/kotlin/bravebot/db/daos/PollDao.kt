package bravebot.db.daos

import bravebot.db.Db
import bravebot.db.jooq.Tables.POLL
import bravebot.db.jooq.Tables.POLL_VOTE
import bravebot.db.jooq.tables.records.PollRecord
import bravebot.db.jooq.tables.records.PollVoteRecord
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject

class PollDao @Inject constructor(db: Db) : Dao(db) {
    fun createPoll(name: String, serverId: Long, channelId: Long, options: Array<String>, endDate: LocalDateTime): PollRecord {
        return dsl.newRecord(POLL).apply {
            this.name = name
            this.serverId = serverId
            this.channelId = channelId
            this.setOptions(*options)
            this.endDt = Timestamp.valueOf(endDate)

            insert()
        }
    }

    fun getAllActive(): List<PollRecord> {
        return dsl.selectFrom(POLL)
                .where(POLL.END_DT.greaterThan(Timestamp.valueOf(LocalDateTime.now())))
                .fetch()
    }

    fun getById(id: Int, serverId: Long): PollRecord? {
        return dsl.selectFrom(POLL)
                .where(POLL.ID.eq(id))
                .and(POLL.SERVER_ID.eq(serverId))
                .fetch()
                .firstOrNull()
    }

    fun getActiveByServer(serverId: Long): List<PollRecord> {
        return dsl.selectFrom(POLL)
                .where(POLL.SERVER_ID.eq(serverId))
                .and(POLL.END_DT.greaterThan(Timestamp.valueOf(LocalDateTime.now())))
                .fetch()
    }

    fun delete(id: Int): Boolean {
        return dsl.deleteFrom(POLL).where(POLL.ID.eq(id)).execute() > 0
    }

    fun createVote(pollId: Int, voterId: Long, choice: Int): PollVoteRecord {
        return dsl.newRecord(POLL_VOTE).apply {
            this.pollId = pollId
            this.voterId = voterId
            this.choice = choice.toShort()

            insert()
        }
    }

    fun getVotesByPoll(pollId: Int): List<PollVoteRecord> {
        return dsl.selectFrom(POLL_VOTE)
                .where(POLL_VOTE.POLL_ID.eq(pollId))
                .fetch()
    }

    fun hasUserVoted(pollId: Int, userId: Long): Boolean {
        return dsl.selectFrom(POLL_VOTE)
                .where(POLL_VOTE.POLL_ID.eq(pollId))
                .and(POLL_VOTE.VOTER_ID.eq(userId))
                .fetch()
                .isNotEmpty
    }
}