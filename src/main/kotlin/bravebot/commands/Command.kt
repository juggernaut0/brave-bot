package bravebot.commands

import bravebot.splitWhitespace
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser

interface Command {
    val helpText: String? get() = null
    fun canHandle(cmdWord: String): Boolean
    fun execute(args: String, author: IUser, channel: IChannel): String
}

abstract class BaseCommand(vararg cmdWords: String) : Command {
    private val cmdWords = cmdWords.map { it.toLowerCase() }.toSet()

    override val helpText = makeHelpText("No description")
    override fun canHandle(cmdWord: String): Boolean = cmdWord in cmdWords

    protected fun makeHelpText(description: String): String {
        val cmds = if (cmdWords.size == 1) cmdWords.first() else cmdWords.joinToString("|", "[", "]")
        return "$cmds - $description"
    }

    protected fun extractSubcommand(args: String): Pair<String?, String> {
        val parts = args.trim().splitWhitespace(limit = 2)
        return when(parts.size) {
            1 -> parts[0] to ""
            2 -> parts[0] to parts[1]
            else -> null to ""
        }
    }
}
