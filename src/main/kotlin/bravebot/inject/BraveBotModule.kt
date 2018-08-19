package bravebot.inject

import bravebot.commands.*
import bravebot.config.BraveBotConfig
import com.google.inject.AbstractModule
import com.google.inject.Provider
import com.google.inject.multibindings.Multibinder
import com.google.inject.name.Names
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.internal.DiscordUtils
import java.util.concurrent.Executors
import javax.sql.DataSource

class BraveBotModule(private val config: BraveBotConfig) : AbstractModule() {
    override fun configure() {
        bindInstance(config)
        bindInstance(dataSource())
        bindInstance(Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory()))
        bindInstance(ClientBuilder().withToken(config.token).build())

        bindCommands()
    }

    private fun bindCommands() {
        Multibinder.newSetBinder(binder(), Command::class.java).apply {
            addBindingTo<CompileCommand>()
            addBindingTo<HelloCommand>()
            addBindingTo<MuricaCommand>()
            addBindingTo<PollCommand>()
            addBindingTo<RollCommand>()
        }
    }

    private fun dataSource(): DataSource {
        val config = HikariConfig().apply {
            dataSourceClassName = config.data.dataSourceClassName

            addDataSourceProperty("user", config.data.user)
            addDataSourceProperty("password", config.data.password)
            addDataSourceProperty("serverName", config.data.host)
            addDataSourceProperty("portNumber", config.data.port.toString())
            addDataSourceProperty("databaseName", config.data.db)
        }
        return HikariDataSource(config)
    }

    private inline fun <reified T> bindInstance(instance: T) = bindInstance(null, instance)
    private inline fun <reified T> bindInstance(name: String?, instance: T) =
            bind(T::class.java)
                    .let { if (name != null) it.annotatedWith(Names.named(name)) else it }
                    .toInstance(instance)
    private inline fun <reified T> provide(crossinline provider: () -> T) =
            bind(T::class.java)
                    .toProvider(Provider<T> { provider() })

    private inline fun <reified T> Multibinder<in T>.addBindingTo() = addBinding().to(T::class.java)
}