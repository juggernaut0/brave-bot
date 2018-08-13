package bravebot

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T> T.getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

inline fun Logger.debug(msg: () -> String) {
    if (isDebugEnabled) debug(msg())
}

inline fun Logger.info(msg: () -> String) {
    if (isInfoEnabled) info(msg())
}
