package bravebot.listeners

import bravebot.getLogger
import bravebot.info
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.ReadyEvent

class ReadyListener : IListener<ReadyEvent> {
    private val log = getLogger()

    override fun handle(event: ReadyEvent) {
        log.info {
            val user = event.client.ourUser
            "Logged in as ${user.name} (${user.stringID})"
        }
    }
}
