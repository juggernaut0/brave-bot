package bravebot.config

class BraveBotConfig(
        val token: String,
        val data: DataConfig
)

class DataConfig(
        val user: String,
        val password: String,
        val host: String,
        val port: Int,
        val db: String,

        val dataSourceClassName: String? = null
)
