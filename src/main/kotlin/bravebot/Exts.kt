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

inline fun Logger.error(e: Throwable? = null, msg: () -> String) {
    if (isErrorEnabled) error(msg(), e)
}

private val whitespace = Regex("\\s+")
fun String.splitWhitespace(limit: Int = 0) = split(whitespace, limit)

private val executorLog = LoggerFactory.getLogger(ScheduledExecutorService::class.java)
fun <T> ScheduledExecutorService.schedule(delay: Duration? = null, block: () -> T): Future<T> {
    val callable = Callable {
        try {
            block()
        } catch (e: Throwable) {
            executorLog.error(e) { "Uncaught error in scheduled execution" }
            throw e
        }
    }
    return schedule(callable, delay?.toMillis() ?: 0, TimeUnit.MILLISECONDS)
}
