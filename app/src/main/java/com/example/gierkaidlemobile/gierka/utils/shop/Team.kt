package utils.shop

import kotlinx.serialization.Serializable

@Serializable
class Team {
    private val teamMembers: MutableList<Programmer> = arrayListOf()


    fun team_addMember(newMember: Programmer) {
        teamMembers.add(newMember)
    }

    override fun toString(): String {
        var retString: String = ""
        for (member in teamMembers) {
            retString += ("$member\n")
        }
        return retString
    }

    fun team_restoreFromString(teamInfo: String) {
        val splitInfo = teamInfo.split('\n')

        for (info in splitInfo) {
            val empty_programer = Programmer()
            empty_programer.stringToProgrammer(info)
            teamMembers.add(empty_programer)
        }
    }

    fun team_getTeamMembers(): MutableList<Programmer> {
        return teamMembers
    }

}