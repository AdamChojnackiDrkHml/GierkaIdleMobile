package concurrency

import Database.DatabaseException
import Database.DatabaseUtils
import GameScreen.gamescreen.GameScreen
import com.badlogic.gdx.Gdx
import resources.NotEnoughResourceException
import resources.ResourceType
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

/**
 * This class manages tasks performed asynchronously regarding the main thread of the game.
 */
object TaskManager {

    private var taskMap : MutableMap<String, PeriodicTask> //a collection of scheduled periodic tasks
    private var taskExecutor : ScheduledThreadPoolExecutor //this object serves as a manager of scheduled tasks
    private lateinit var asyncConnection : Future<Boolean>

    init {
        taskMap = getTaskMap()
        taskExecutor = ScheduledThreadPoolExecutor(taskMap.size)
    }

    /**
     * Performs asynchronous connection to the database and restores the game state.
     */
    fun asyncConnectToDB() {
        asyncConnection = taskExecutor.submit({
            DatabaseUtils.database_login()
        }, true)
    }

    /**
     * This function starts all the scheduled tasks defined in the constructor as soon as
     * connection with the database is acquired by the asyncConnectToDB() function.
     */
    fun startTasks() {
        thread(isDaemon = true) {
            try {
                //TODO: inform user about waiting for connection with the database (Please wait...) and notify the front-end when the connection is acquired
                Logger.logInfo("Please wait for connection with the database...")
                if(asyncConnection.get()) { //blocks, waiting for connection or starts immediately if it is already acquired
                    Logger.logInfo("Connection acquired, starting tasks")
                    try {
                        GameStateMonitor.restoreGamestate()
                    } catch(e : DatabaseException) {
                        Logger.logInfo(e.toString())
                    }
                    taskMap.forEach {
                        it.component2().apply {
                            taskExecutor.scheduleAtFixedRate(task, delay, period, unit)
                        }
                    }
                } else {
                    GameScreen.showErrorMessage("Failed to restore the game state from database - connection failure")
                }
            } catch (e : ExecutionException) {
                GameScreen.showErrorMessage("Failed to restore the game state from database - connection failure")
            } catch (e : InterruptedException) {
                Logger.logError(e.toString())
            }
        }
    }

    /**
     * Initializes new contract.
     * @param difficulty the difficulty of a contract.
     */
    fun initializeContract(difficulty: Int) = GameStateMonitor.handleContractRequest(difficulty)

    fun endContract() {
        thread(isDaemon = false) {
            val result : Boolean = GameStateMonitor.finalizeContract()
            Gdx.app.postRunnable {
                GameScreen.showContractStatusMessage(
                    if(result) {
                        "Contract accomplished!"
                    } else {
                        "Contract failed!"
                    }
                )
            }
        }
    }

    fun finalize() {
            taskExecutor.shutdownNow()
            GameStateMonitor.saveGamestate()
            Logger.logInfo("Save on exit")
    }

    private fun getTaskMap() : MutableMap<String, PeriodicTask> = mutableMapOf(
        //this task updates GameState, based on how many times player clicked the laptop
        "UPDATE_CLICK_COUNT" to PeriodicTask({
            GameStateMonitor.updateClickCount()
        },0, 200, TimeUnit.MILLISECONDS),

        "UPDATE_PASSIVE_INCOME" to PeriodicTask({
            GameStateMonitor.updatePassiveIncome()
        }, 0, 1000, TimeUnit.MILLISECONDS),

        //this task gets the current GameState and updates the corresponding graphics
        "UPDATE_DISPLAY" to PeriodicTask({
            val resources = GameStateMonitor.getResourceCount()
            Gdx.app.postRunnable {
                GameScreen.run {
                    resources.run {
                        updateCurrentLineCount(lines)
                        updateCurrentCashCount(cash)
                        updateCurrentCaffeineCount(caffeine)
                    }
                }
            }
        }, 0, 200, TimeUnit.MILLISECONDS),

        //this task is supposed to update the game state in database
        "UPDATE_DATABASE" to PeriodicTask({
            try {
                GameStateMonitor.saveGamestate()
                Logger.logInfo("Save to database successful")
            } catch(e : DatabaseException) {
                GameScreen.showErrorMessage("Failed to save to database - database error.")
            }
        }, 5, 5, TimeUnit.SECONDS)
    )

}