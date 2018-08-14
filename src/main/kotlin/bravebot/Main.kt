package bravebot

import bravebot.commands.CompileCommand
import bravebot.commands.HelloCommand
import bravebot.commands.MuricaCommand
import bravebot.commands.PollCommand
import bravebot.config.BraveBotConfig
import bravebot.config.DataConfig
import bravebot.db.Db
import bravebot.db.daos.PollDao
import bravebot.listeners.MessageReceivedListener
import bravebot.listeners.ReadyListener
import bravebot.listeners.ShutdownListener
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.internal.DiscordUtils
import java.io.File
import java.util.concurrent.Executors

fun main(args: Array<String>) {
    if (args.isEmpty()) throw IllegalArgumentException("Config file required")
    // TODO env var substitution
    val mapper = YAMLMapper().registerKotlinModule()
    val config = mapper.readValue<BraveBotConfig>(File(args[0]))

    val dataSource = getDataSource(config.data)
    val db = Db(dataSource)

    val client = ClientBuilder().withToken(config.token).build()
    with (client.dispatcher) {
        registerListener(ReadyListener())
        registerListener(ShutdownListener(dataSource))
        registerListener(MessageReceivedListener(listOf(
                HelloCommand(),
                CompileCommand(),
                MuricaCommand(),
                PollCommand(PollDao(db), Executors.newSingleThreadScheduledExecutor(DiscordUtils.createDaemonThreadFactory()))
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
