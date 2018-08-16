package bravebot.listeners

import bravebot.error
import bravebot.getLogger
import com.zaxxer.hikari.HikariDataSource
import sx.blah.discord.api.events.Event
import sx.blah.discord.api.events.IListener
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.sql.DataSource

class ShutdownEvent : Event()

class ShutdownListener @Inject constructor(private val dataSource: DataSource) : IListener<ShutdownEvent> {
    private val log = getLogger()

    override fun handle(event: ShutdownEvent) {
        Executors.newSingleThreadExecutor().apply {
            execute {
                try { event.client.logout() } catch (e: Throwable) { log.error(e) { "" } }
                try { (dataSource as? HikariDataSource)?.close() } catch (e: Throwable) { log.error(e) { "" } }
            }
            shutdown()
        }
    }
}