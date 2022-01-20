package concurrency

import Database.DatabaseUtils
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
    private val gamestate : Gamestate = Gamestate().apply { setDefaultValues() }
    private val clickCount : AtomicInteger = AtomicInteger(0)
    private val gamestateLock : ReentrantLock = ReentrantLock()
    private val cashMock : AtomicInteger = AtomicInteger(3000)
    private val caffeineMock : AtomicInteger = AtomicInteger(0)
    private var currentPassiveIncome : Long = 100
    private var linesPerClick : Long = 2

    /**
     * Atomically increases the total number of clicks between updates of the GameState object.
     */
    fun incrementClickCount() {
        clickCount.incrementAndGet()
    }

    /**
     * Reads the total number of clicks, atomically resets the counter and also atomically updates the GameState object.
     */
    fun updateClickCount() {
        val amount : Int = clickCount.getAndSet(0)
        gamestateLock.withLock {
            gamestate.setGamestate(linesPerClick.toInt(), amount)
        }
    }

    fun updatePassiveIncome() = gamestateLock.withLock {
        gamestate.addPassiveIncome(currentPassiveIncome.toInt())
    }

    /**
     * @return current total number of lines
     * Mutually exclusively reads the current total number of lines from the GameState object.
     */
    fun getResourceCount() : ResourceCount = gamestateLock.withLock {
            ResourceCount(gamestate.getGamestate(), cashMock.get(), caffeineMock.get())
    }

    /**
     * Saves current game state to the database.
     */
    fun saveGamestate() {
        val json : JsonObject
        gamestateLock.withLock {
            json = JsonObject(gamestate.toJson())
        }
        DatabaseUtils.database_updateGamestate(json)
    }

    /**
     * Loads game state from the database.
     */
    fun restoreGamestate() {
        val jsonString : String = DatabaseUtils.database_getGamestate().json
        gamestateLock.withLock {
            gamestate.fromJson(jsonString)
        }
    }

    fun getTeamMembers() : MutableList<Programmer> = gamestateLock.withLock { gamestate.gamestateData.team.team_getTeamMembers() }

    fun buyUpgrade(bonus : Int, price : Int) {
        var success = false
        cashMock.updateAndGet {
            if(it >= price) {
                success = true
                it - price
            } else {
                it
            }
        }
        if(success) {
            linesPerClick += bonus
            gamestateLock.withLock {
                //update the tier of upgrade in game state
            }
        } else {
            throw NotEnoughResourceException(ResourceType.CASH)
        }
    }

    fun hireProgrammer(programmer : Programmer) {
        programmer.run {
            val price = getPrice()
            var success = false
            cashMock.updateAndGet {
                if(it >= price) {
                    success = true
                    it - price
                } else {
                    it
                }
            }
            if(success) {
                currentPassiveIncome += getLinesPerSecond()
                gamestateLock.withLock {
                    gamestate.gamestateData.team.team_addMember(programmer)
                }
            } else {
                throw NotEnoughResourceException(ResourceType.CASH)
            }
        }
    }

    fun buyCaffeine(amount : Int, price : Int) {
        var success = false
        cashMock.updateAndGet {
            if(it >= price) {
                success = true
                it - price
            } else {
                it
            }
        }
        if(success) {
            caffeineMock.addAndGet(amount)
        } else {
            throw NotEnoughResourceException(ResourceType.CASH)
        }
    }

}