package concurrency

import java.util.concurrent.TimeUnit

/**
 * This data class represents a periodic task used by TaskManager.
 */
data class PeriodicTask(
    val task: Runnable,
    val delay: Long,
    var period: Long,
    var unit: TimeUnit)
