package bravebot

import bravebot.commands.*
import bravebot.config.DataConfig
import bravebot.config.loadConfig
import bravebot.db.Db
import bravebot.db.daos.PollDao
import bravebot.listeners.MessageReceivedListener
import bravebot.listeners.ReadyListener
import bravebot.listeners.ShutdownListener
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.internal.DiscordUtils
import java.nio.file.Paths
import java.util.concurrent.Executors

fun main(args: Array<String>) {
    if (args.isEmpty()) throw IllegalArgumentException("Config file required")

    val config = loadConfig(Paths.get(args[0]))

    val dataSource = getDataSource(config.data)
    val db = Db(config.data, dataSource)

    val executor = Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory())

    val client = ClientBuilder().withToken(config.token).build()
    with (client.dispatcher) {
        registerListener(ReadyListener())
        registerListener(ShutdownListener(dataSource))
        registerListener(MessageReceivedListener(listOf(
                HelloCommand(),
                CompileCommand(),
                MuricaCommand(),
                PollCommand(PollDao(db), executor)
        )))
    }
    client.login()
}

private fun getDataSource(dataConfig: DataConfig): HikariDataSource {
    val config = HikariConfig().apply {
        dataSourceClassName = dataConfig.dataSourceClassName

        addDataSourceProperty("user", dataConfig.user)
        addDataSourceProperty("password", dataConfig.password)
        addDataSourceProperty("serverName", dataConfig.host)
        addDataSourceProperty("portNumber", dataConfig.port.toString())
        addDataSourceProperty("databaseName", dataConfig.db)
    }
    return HikariDataSource(config)
}
