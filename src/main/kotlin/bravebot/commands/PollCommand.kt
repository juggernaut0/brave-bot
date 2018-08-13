package bravebot.commands

import bravebot.db.daos.PollDao
import bravebot.schedule
import bravebot.splitWhitespace
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ScheduledExecutorService

class PollCommand(private val dao: PollDao,
                  private val executor: ScheduledExecutorService
) : BaseCommand("poll") {
    override val helpText = makeHelpText("Create polls and cast your vote")

    override fun execute(args: String, author: IUser, channel: IChannel): String {
        if (channel.isPrivate) return "You can't use polls in a private channel!"

        val (subcmd, subargs) = extractSubcommand(args)
        return when (subcmd) {
            "create" -> createPoll(subargs, channel)
            "vote" -> "TODO votes" //TODO
            else -> "TODO list polls" // TODO
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
            dao.createPoll(name, channel.guild.longID, options, endDate)
        }

        executor.schedule(dur) {
            endPoll(poll.id, channel)
        }

        return "Poll created with id ${poll.id}."
    }

    private fun endPoll(pollId: Int, channel: IChannel) {
        TODO()
    }

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

    companion object {
        private const val createUsage = "Usage: 'create [time]' followed by poll title and options."
    }
}