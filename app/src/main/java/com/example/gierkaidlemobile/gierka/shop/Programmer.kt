package utils.shop

import kotlinx.serialization.Serializable

@Serializable
class Programmer() {
    private var tier: Int = 0
    private var linesPerSec: Int = 0
    private var currentLPS: Int = 0
    private var cost: Int = 0
    private var caffMod: Int = 0
    private var caffeine: Int = 0
    private var imageFile: String = "default.png"

    /**
     * Losowo generowane statystyki programisty oraz adekwatny do nich koszt.
     */
    constructor(tier: Int) : this() {
        this.tier = tier
        val parLines = (1..10).random()
        val parCaff = (1..5).random()
        linesPerSec = parLines * (tier + 1) * (tier + 1) + 10 * tier * tier * tier
        currentLPS = linesPerSec
        caffMod = parCaff * (tier + 1)
        caffeine = 0
        cost = 10 * (tier + 1) * (tier + 1) * (parLines + parCaff)
    }

    /**
     * Czytanie programmera z bazy danych
     */
    constructor(string: String) : this() {
        val programmer = string.split(" ").toTypedArray()
        tier = programmer[0].toInt()
        linesPerSec = programmer[1].toInt()
        currentLPS = programmer[2].toInt()
        cost = programmer[3].toInt()
        caffMod = programmer[4].toInt()
        caffeine = programmer[5].toInt()
        imageFile = programmer[6]
    }

    /**
     * Tlumaczenie waznych informacji dla bazy danych
     * Nalezy aktualizowac wraz z rozbudowywaniem klasy Programer
     */
    override fun toString(): String {
        return "$tier $linesPerSec $currentLPS $cost $caffMod $caffeine $imageFile"
    }

    /**
     * Nadpisanie pol programisty do wartosci z bazy danych
     * Zwraca true jesli nadpisanie sie powiedzie
     */
    fun stringToProgrammer(string: String): Boolean {
        val programmer = string.split(" ").toTypedArray()
        val tmpTier: Int
        val tmpLinesPerSec: Int
        val tmpCurrentLPS: Int
        val tmpCost: Int
        val tmpCaffMod: Int
        val tmpCaffeine: Int
        val tmpImageFile: String
        try {
            tmpTier = programmer[0].toInt()
            tmpLinesPerSec = programmer[1].toInt()
            tmpCurrentLPS = programmer[2].toInt()
            tmpCost = programmer[3].toInt()
            tmpCaffMod = programmer[4].toInt()
            tmpCaffeine = programmer[5].toInt()
            tmpImageFile = programmer[6]
        }
        catch (e: Exception) {
            return false
        }
        tier = tmpTier
        linesPerSec = tmpLinesPerSec
        currentLPS = tmpCurrentLPS
        cost = tmpCost
        caffMod = tmpCaffMod
        caffeine = tmpCaffeine
        imageFile = tmpImageFile
        return true
    }

    /**
     * Ustawienie obrazka (avatara) dla programisty; estetyczne
     */
    fun setImageFilename(filename: String) {
        imageFile = filename
    }

    /**
     * LPS with added caffeine modification
     */
    fun updateCurrentLPS() {
        currentLPS = linesPerSec + caffeine * caffMod
    }

    /**
     * Funkcje do nanoszenia zmian do liczby kofeiny ktora nakarmiono danego Programiste
     */
    fun setCaffeineAmount(amount: Int) {
        caffeine = amount
        updateCurrentLPS()
    }

    fun incrementCaffeineAmount() {
        caffeine += 1
        updateCurrentLPS()
    }

    /**
     * Gettery do podstawowych informacji
     */
    fun getCurrentLPS(): Int {
        return currentLPS
    }

    fun getLinesPerSecond(): Int {
        return linesPerSec
    }

    fun getPrice(): Int {
        return cost
    }

    fun getCaffeineMod(): Int {
        return caffMod
    }

    fun getImageFilename(): String {
        return imageFile
    }

    fun getCaffeineAmount(): Int {
        return caffeine
    }
}