package concurrency

import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object Logger {
    private val logger = LoggerFactory.getLogger("")
    private val lock = ReentrantLock()

    fun logInfo(msg : String) {
        lock.withLock {
            logger.info(msg)
        }
    }

    fun logError(msg : String) {
        lock.withLock {
            logger.error(msg)
        }
    }

}