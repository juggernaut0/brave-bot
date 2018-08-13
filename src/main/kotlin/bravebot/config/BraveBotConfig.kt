package bravebot.config

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.commons.text.StringSubstitutor
import org.jooq.SQLDialect
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

fun loadConfig(path: Path): BraveBotConfig {
    val subst = StringSubstitutor(System.getenv())
    val src = String(Files.readAllBytes(path), Charset.defaultCharset())
    val mapper = YAMLMapper().registerKotlinModule()
    return mapper.readValue(subst.replace(src))
}

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

        val dataSourceClassName: String,
        val sqlDialect: SQLDialect
)
