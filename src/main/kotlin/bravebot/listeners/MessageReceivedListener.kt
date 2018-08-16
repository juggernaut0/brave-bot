package bravebot.listeners

import bravebot.commands.Command
import bravebot.debug
import bravebot.getLogger
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import javax.inject.Inject

class MessageReceivedListener @Inject constructor(
        private val commands: MutableSet<Command> // must be mutable for guice multibinding
) : IListener<MessageReceivedEvent> {
    private val log = getLogger()

    override fun handle(event: MessageReceivedEvent) {
        if (event.author == event.client.ourUser) return

        val (rawCmd, args) = when {
            event.channel.isPrivate -> {
                log.debug { "Private channel: ${event.message.content}" }
                extractCmd(event.message.content)
            }
            event.client.ourUser in event.message.mentions -> {
                log.debug { "Channel ${event.channel.stringID}: ${event.message.content}" }
                val afterMention = event.message.content.substringAfter("${event.client.ourUser.stringID}>").trim()
                extractCmd(afterMention)
            }
            else -> return
        }

        if (rawCmd != null) {
            val cmd = rawCmd.toLowerCase().trim('.', ',', '?', '!', ':', ';', '\\', '"', '\'').trim()

            // special cases
            val resp = if (cmd == "help") {
                buildString {
                    append("Available commands:\n")
                    for (command in commands) {
                        append(command.helpText)
                        append('\n')
                    }
                }
            } else if (cmd == "logout" && event.author.longID == 134231786682056704) {
                event.client.dispatcher.dispatch(ShutdownEvent())
                "Shutting down..."
            } else {
                commands.find { it.canHandle(cmd) }?.execute(args, event.author, event.channel) ?: "Unknown command $cmd"
            }

            event.channel.sendMessage(resp)
        }
    }

    private fun extractCmd(msg: String): Pair<String?, String> {
        val parts = msg.trim().split(Regex("\\s"), limit = 2)
        return when {
            parts.isEmpty() -> null to ""
            parts.size == 1 -> parts[0] to ""
            else -> parts[0] to parts[1]
        }
    }
}
