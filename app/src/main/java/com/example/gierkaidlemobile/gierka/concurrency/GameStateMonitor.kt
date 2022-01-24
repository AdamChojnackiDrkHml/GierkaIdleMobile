package concurrency

import Database.DatabaseException
import Database.DatabaseUtils
import GameScreen.IdleGame
import GameScreen.gamescreen.GameScreen
import gamestate.Gamestate
import org.bson.json.JsonObject
import resources.NotEnoughResourceException
import resources.ResourceCount
import resources.ResourceType
import utils.shop.Programmer
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * This class serves as a monitor object, whenever we want to perform a read/write from/to the GameState or update the current number of clicks.
 */
object GameStateMonitor {
    private val gamestate: Gamestate = Gamestate()
    private val contractHandler: ContractHandler = ContractHandler()
    private val clickCount: AtomicInteger = AtomicInteger(0)
    private val gamestateLock: ReentrantLock = ReentrantLock()
    private lateinit var game: IdleGame

    fun init(game: IdleGame) {
        this.game = game
    }

    /**
     * Atomically increases the total number of clicks between updates of the GameState object.
     */
    fun incrementClickCount() {
        clickCount.incrementAndGet()
        contractHandler.click()
    }

    /**
     * Reads the total number of clicks, atomically resets the counter and also atomically updates the GameState object.
     */
    fun updateClickCount() {
        val amount: Int = clickCount.getAndSet(0)
        gamestateLock.withLock {
            gamestate.setGamestate(amount)
        }
    }

    fun updatePassiveIncome() = gamestateLock.withLock {
        gamestate.addPassiveIncome()
    }

    /**
     * @return current total number of lines
     * Mutually exclusively reads the current total number of lines from the GameState object.
     */
    fun getResourceCount(): ResourceCount = gamestateLock.withLock {
        ResourceCount(gamestate.getGamestate(), gamestate.gamestateData.money, gamestate.gamestateData.caffeine)
    }

    /**
     * Saves current game state to the database.
     */
    fun saveGamestate() {
        val json: JsonObject
        gamestateLock.withLock {
            json = JsonObject(gamestate.toJson())
        }
        DatabaseUtils.database_updateGamestate(json)
    }

    /**
     * Loads game state from the database.
     */
    fun restoreGamestate() {
        try {
            val jsonString: String = DatabaseUtils.database_getGamestate().json
            gamestateLock.withLock {
                gamestate.fromJson(jsonString)
            }
        } catch (e: DatabaseException) {
            Logger.logInfo("No save yet, set default values")
            gamestateLock.withLock {
                gamestate.setDefaultValues()
            }
        } catch (e: Exception) {
            Logger.logError(e.toString())
            gamestateLock.withLock {
                gamestate.setDefaultValues()
            }
        } finally {
            var upgradesOffer : Triple<IntArray, IntArray, IntArray>
            gamestateLock.withLock {
                val upgradesArray = gamestate.gamestateData.upgrades
                val size = upgradesArray.size
                upgradesOffer = Triple(IntArray(size), IntArray(size), IntArray(size))
                var i = 0
                upgradesArray.forEach{
                    upgradesOffer.first[i] = it[1]
                    upgradesOffer.second[i] = it[2]
                    upgradesOffer.third[i] = it[3]
                    i++
                }
            }
            (game.screen as GameScreen).updateUpgradeOffer(upgradesOffer)
            notifyCostsChanged()
        }
    }

    fun getTeamMembers(): MutableList<Programmer> =
        gamestateLock.withLock { gamestate.gamestateData.team.team_getTeamMembers() }

    fun buyUpgrade(type: Int, price: Int) {
        if (gamestateLock.withLock {
                gamestate.purchaseUpgradeFromStore(type, price)
            }) {
            notifyCostsChanged()
        } else {
            throw NotEnoughResourceException(ResourceType.CASH)
        }
    }

    fun hireProgrammer(programmer: Programmer) {
        if (!gamestateLock.withLock {
                gamestate.hireProgrammer(programmer)
            }) {
            throw NotEnoughResourceException(ResourceType.CASH)
        }
    }

    fun buyCaffeine(amount: Int, price: Int) {
        if (!gamestateLock.withLock {
                gamestate.buyCaffeine(amount, price)
            }) {
            throw NotEnoughResourceException(ResourceType.CASH)
        }
    }

    fun handleContractRequest(difficulty: Int) {
        if(gamestateLock.withLock {
                gamestate.buyContract(difficulty)
        }) {
            contractHandler.initialize(difficulty)
        } else {
            throw NotEnoughResourceException(ResourceType.CODE)
        }
    }

    fun finalizeContract() : Boolean = contractHandler.finalize()

    private fun notifyCostsChanged() = gamestateLock.withLock {
            (game.screen as GameScreen).updateContractCosts(Triple(
                gamestate.getContractCost(0),
                gamestate.getContractCost(1),
                gamestate.getContractCost(2)
            ))
        }

    class ContractHandler {
        private var clickCount : Int = 0
        private var target : Int = 0
        private var isExecuting : Boolean = false

        fun click() {
            if(isExecuting) {
                clickCount++
                (game.screen as GameScreen).updateContractCompletionLabel(clickCount.toFloat() / target)
            }
        }

        fun initialize(difficulty : Int) {
            isExecuting = true
            (game.screen as GameScreen).updateContractCompletionLabel(0.0F)
            target = gamestate.startContract(difficulty) / 10
            Logger.logInfo("Start contract: $target")
        }

        fun finalize() : Boolean {
            val result = clickCount >= target
            gamestateLock.withLock {
                gamestate.endContract(result)
            }
            isExecuting = false
            Logger.logInfo("End contract: $clickCount")
            clickCount = 0
            notifyCostsChanged()
            return result
        }

    }

}