package utils.shop

import gamestate.Gamestate

class Shop {
    private var gameState: Gamestate? = null

    /**
     * Wartosci ponizszych pol pobierane sa ze statycznych metod klas Upgrades, Caffeine.
     * Lista ofert programistow wykorzystuje klase Programmer
     */
    private var upgradeTiers: IntArray = Upgrades.getTiers()
    private var upgradeValues: IntArray = Upgrades.getValues()
    private var upgradeCosts: IntArray = Upgrades.getCosts()
    private val upgradeNames = Upgrades.getNames()

    private var programmerOffer = MutableList(3) { Programmer(1) }

    private var caffeineOffers: IntArray = Caffeine.getOffers()
    private var caffeineCosts: IntArray = Caffeine.getCosts()
    private val caffeineNames = Caffeine.getNames()

    fun Shop(gs: Gamestate?) {
        gameState = gs
        upgradeValues = Upgrades.calcValues(upgradeTiers)
        upgradeCosts = Upgrades.calcCosts(upgradeTiers)
    }

    /**
     * Sluzy do aktualizacji jesli wartosci w gameState zmienia sie z innego powodu niz funkcje tej klasy
     */
    fun updateGameState(gs: Gamestate?) {
        gameState = gs
    }

    /**
     * Po dokonanym zakupie ulepszenia podwyzsza sie Tier tegoz ulepszenia
     */
    fun incrementUpgradeTier(index: Int) {
        upgradeTiers[index]++
        upgradeValues = Upgrades.calcValues(upgradeTiers)
        upgradeCosts = Upgrades.calcCosts(upgradeTiers)
    }

    /**
     * Sluzy do pobrania pojedynczej nazwy ulepszenia do frontendu
     */
    fun getUpgradeName(index: Int): String {
        return upgradeNames[index]
    }

    /**
     * Sluzy do pobrania wszystkich nazw ulepszen do frontendu
     */
    fun getUpgradeNamesList(): Array<String> {
        return upgradeNames
    }

    /**
     * Przekazuje frontendowi oferte ulepszen
     */
    fun showUpgradesOffer(): Triple<IntArray, IntArray, IntArray> {
        upgradeValues = Upgrades.calcValues(upgradeTiers)
        upgradeCosts = Upgrades.calcCosts(upgradeTiers)
        return Triple(upgradeTiers, upgradeValues, upgradeCosts)
    }


    /**
     * generowanie oferty z losowych programistow na podstawie Tieru oraz n - liczby programistow w ofercie
     */
    fun generateProgrammerOffer(n: Int, tier: Int) {
        programmerOffer = MutableList(n) { Programmer(tier) }
    }

    /**
     * Przekazuje frontendowi oferte programistow
     */

    fun showProgrammerOffer(): MutableList<Programmer> {
        return programmerOffer

    }

    /**
     * Po zatrudnieniu programisty nalezy usunac go z oferty
     */
    fun removeProgrammerFromOffer(index: Int) {
        if (programmerOffer.size > index)
            programmerOffer.removeAt(index)
    }


    /**
     * Sluzy do pobrania pojedynczej nazwy oferty kofeinowej do frontendu
     */
    fun getCaffeineName(index: Int): String {
        return caffeineNames[index]
    }

    /**
     * Sluzy do pobrania wszystkich nazw ofert kofeinowych do frontendu
     */
    fun getCaffeineNamesList(): Array<String> {
        return caffeineNames
    }

    /**
     * Przekazuje frontendowi oferty kofeinowe
     */
    fun showCaffeineOffer(): Pair<IntArray, IntArray> {
        return Pair(caffeineOffers, caffeineCosts)
    }
}