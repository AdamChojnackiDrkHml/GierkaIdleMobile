package gamestate
import com.mongodb.event.CommandSucceededEvent
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import utils.shop.Contracts
import utils.shop.Team
import java.util.*
import kotlin.collections.ArrayList

@Serializable
data class GamestateData(
        var upgrades: ArrayList<IntArray>,
        var linesOfCode: IntArray,
        var linesPerSecond: Int,
        var linesPerClick: Int,
        var contracts: Contracts,
        var team: Team,
        val linesPerSecondDefault: Int,
        val linesPerClickDefault: Int,
        var money: Int,
        var caffeine: Int

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GamestateData

        if (!linesOfCode.contentEquals(other.linesOfCode)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = upgrades.hashCode()
        result = 31 * result + linesOfCode.contentHashCode()
        result = 31 * result + linesPerSecond
        result = 31 * result + linesPerClick
        result = 31 * result + contracts.hashCode()
        result = 31 * result + team.hashCode()
        return result
    }

}


