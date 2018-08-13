package bravebot

import bravebot.commands.Command
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

class ReadyListener : IListener<ReadyEvent> {
    private val log = getLogger()

    override fun handle(event: ReadyEvent) {
        log.info {
            val user = event.client.ourUser
            "Logged in as ${user.name} (${user.stringID})"
        }
    }
}

class MessageReceivedListener(private val commands: List<Command> = emptyList()) : IListener<MessageReceivedEvent> {
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
                event.client.logout()
                return
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
