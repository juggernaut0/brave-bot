package bravebot

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

inline fun <reified T> T.getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

inline fun Logger.debug(msg: () -> String) {
    if (isDebugEnabled) debug(msg())
}

inline fun Logger.info(msg: () -> String) {
    if (isInfoEnabled) info(msg())
}

private val whitespace = Regex("\\s+")
fun String.splitWhitespace(limit: Int = 0) = split(whitespace, limit)

fun <T> ScheduledExecutorService.schedule(delay: Duration? = null, block: () -> T): Future<T> {
    return schedule(Callable { block() }, delay?.toMillis() ?: 0, TimeUnit.MILLISECONDS)
}
