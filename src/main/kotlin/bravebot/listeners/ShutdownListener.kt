package bravebot.listeners

import com.zaxxer.hikari.HikariDataSource
import sx.blah.discord.api.events.Event
import sx.blah.discord.api.events.IListener
import java.util.concurrent.Executors

class ShutdownEvent : Event()

class ShutdownListener(private val dataSource: HikariDataSource) : IListener<ShutdownEvent> {
    override fun handle(event: ShutdownEvent) {
        Executors.newSingleThreadExecutor().apply {
            execute {
                event.client.logout()
                dataSource.close()
            }
            shutdown()
        }
    }
}