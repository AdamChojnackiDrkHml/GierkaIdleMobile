package utils.shop

import kotlinx.serialization.Serializable

@Serializable
class Contracts {
    var easyMode: Int = 15
    var mediumMode: Int = 20
    var hardMode: Int = 25
    var timesFailed: Int = 0
    var timesSucceeded: Int = 0

    /**
     * Sprawdza czy poziom trudności jest odpowiedni na podstawie poprzednich rezultatów kontraktów
     */
    fun checkDiff(){
        if(timesFailed == 3) decreaseDifficulty()
        if(timesSucceeded == 3) increaseDifficulty()
    }

    /**
     * Zwiększa ilość kliknięć/sekunde wymaganych do wypełnienia kontraktu
     */
    private fun increaseDifficulty() {
        timesSucceeded=0
        val oldHard = hardMode
        val oldMedium = mediumMode
        hardMode += 2
        mediumMode = oldHard
        easyMode = oldMedium
    }

    /**
     * Zmniejsza ilość kliknięć/sekunde wymaganych do wypełnienia kontraktu
     */
    private fun decreaseDifficulty(){
        timesFailed=0
        if(easyMode - 2 > 1){
            val oldEasy = easyMode
            val oldMedium = mediumMode
            easyMode -= 2
            mediumMode = oldEasy
            hardMode = oldMedium
        }
        else{
            easyMode = 11
            mediumMode = 13
            hardMode = 15
        }
    }
}