package bravebot.commands

import bravebot.db.daos.PollDao
import bravebot.error
import bravebot.getLogger
import bravebot.schedule
import bravebot.splitWhitespace
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ScheduledExecutorService
import javax.inject.Inject

class PollCommand @Inject constructor(
        private val dao: PollDao,
        private val executor: ScheduledExecutorService,
        client: IDiscordClient
) : BaseCommand("poll") {
    private val log = getLogger()

    override val helpText = makeHelpText("Create polls and cast your vote")

    init {
        dao.tx {
            for (poll in dao.getAllActive()) {
                val dur = Duration.between(LocalDateTime.now(), poll.endDt.toLocalDateTime())
                val pollId = poll.id
                val channelId = poll.channelId
                executor.schedule(dur) {
                    endPoll(pollId, client.getChannelByID(channelId))
                }
            }
        }
    }

    override fun execute(args: String, author: IUser, channel: IChannel): String {
        if (channel.isPrivate) return "You can't use polls in a private channel!"

        val (subcmd, subargs) = extractSubcommand(args)
        return when (subcmd) {
            "create" -> createPoll(subargs, channel)
            "vote" -> vote(subargs, author.longID, channel.guild.longID)
            null, "", "list" -> listPolls(channel.guild.longID)
            else -> "Unknown poll command $subcmd. Valid options: create, vote, list"
        }
    }

    private fun createPoll(argStr: String, channel: IChannel): String {
        val lines = argStr.lines()
        if (lines.size < 2) return createUsage

        val dur = parseTime(lines[0]) ?: return "Could not parse ${lines[0]} into a time."
        val endDate = LocalDateTime.now().plus(dur)

        val name = lines[1]
        val options = lines.subList(2, lines.size).toTypedArray()

        val poll = dao.tx {
            dao.createPoll(name, channel.guild.longID, channel.longID, options, endDate)
        }

        executor.schedule(dur) {
            endPoll(poll.id, channel)
        }

        return "Poll created with id ${poll.id}."
    }

    private fun endPoll(pollId: Int, channel: IChannel) {
        val msg = dao.tx {
            val poll = dao.getById(pollId, channel.guild.longID) ?: return@tx null
            val votes = dao.getVotesByPoll(pollId)
                    .groupBy { it.choice }
                    .mapValues { (_, v) -> v.size }

            dao.delete(pollId)

            buildString {
                appendln("Poll '${poll.name}' has ended. Results:")
                for ((i, count) in votes.entries.sortedBy { it.value }) {
                    val option = poll.options[i.toInt()]
                    appendln("$count -- $option")
                }
            }
        }

        if (msg != null) {
            channel.sendMessage(msg)
        } else {
            log.error { "Unable to end poll ID $pollId on channel ID ${channel.longID}" }
        }
    }

    private fun listPolls(serverId: Long): String {
        val polls = dao.tx { dao.getActiveByServer(serverId) }
        return if (polls.isEmpty()) "No active polls."
        else buildString {
            appendln("Active polls:")
            for (poll in polls) {
                appendln("- ${poll.name} (ID: ${poll.id})")
            }
        }
    }

    private fun vote(argStr: String, userId: Long, serverId: Long): String {
        val (idStr, option) = argStr.splitWhitespace(limit = 2).takeIf { it.size == 2 } ?: return voteUsage
        val pollId = idStr.toIntOrNull() ?: return voteUsage
        return dao.tx {
            val poll = dao.getById(pollId, serverId) ?: return@tx "Poll with ID $pollId not found."

            if (dao.hasUserVoted(pollId, userId)) return@tx "You've already voted for this poll."

            val choice = poll.options
                    .indexOfFirst { it.startsWith(option, ignoreCase = true) }
                    .takeIf { it >= 0 } ?: return@tx "I don't recognize '$option' as an option for that poll."
            dao.createVote(poll.id, userId, choice)
            "Vote counted!"
        }
    }

    companion object {
        private const val createUsage = "Usage: 'poll create [time]' followed by poll title and options."
        private const val voteUsage = "Usage: 'poll vote [id] [option]'"

        private fun parseTime(timeStr: String): Duration? {
            if (timeStr == "forever") return Duration.ofDays(1000000)
            val (numStr, unitStr) = timeStr.splitWhitespace().takeIf { it.size >= 2 }?.let { it[0] to it[1] } ?: return null
            val num = numStr.toLongOrNull() ?: return null
            val unit = when (unitStr) {
                "m", "min", "mins", "minute", "minutes" -> ChronoUnit.MINUTES
                "h", "hour", "hours" -> ChronoUnit.HOURS
                "d", "day", "days" -> ChronoUnit.DAYS
                "w", "week", "weeks" -> ChronoUnit.WEEKS
                else -> return null
            }
            return Duration.of(num, unit)
        }
    }
}