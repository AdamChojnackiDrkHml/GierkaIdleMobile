package gamestate

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import utils.shop.Contracts
import utils.shop.Programmer
import utils.shop.Team
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

class Gamestate internal constructor() {

    private var timesClicked = 0

    private var contractDifficulty = 0
    private val contractTime: Int = 30

    lateinit var gamestateData: GamestateData


    /**
     * Funkcja ustawia bazowe wartości, trzeba ją wywołać jak jest nowe konto
     */
    fun setDefaultValues(){

        val default = ArrayList<IntArray>()

        val linesOfCode = IntArray(10)
        val linesPerSecond = 100
        val linesPerClick = 20
        val contract = Contracts()
        val team = Team()

//        0 LPS
//        1 LPC
//        2 CRIT
//        3 LPS FOR TEAM MODIFIER
//        4 ADDITIONAL PROGRAMMER
//        5 MONEY FROM PROJECTS
//        6 MORE CAFFEINE

        val lpsArr = IntArray(4)
        lpsArr[0] = 0
        lpsArr[3] = 200
        default.add(lpsArr)

        val lpcArr = IntArray(4)
        lpcArr[0] = 1
        lpcArr[3] = 200
        default.add(lpcArr)

        val critArr = IntArray(4)
        critArr[0] = 2
        critArr[3] = 200
        default.add(critArr)

        val teamLpsArr = IntArray(4)
        teamLpsArr[0] = 3
        teamLpsArr[3] = 200
        default.add(teamLpsArr)

        val progSpaceArr = IntArray(4)
        progSpaceArr[0] = 4
        progSpaceArr[2] = 2
        progSpaceArr[3] = 200
        default.add(progSpaceArr)

        val projMoneyArr = IntArray(4)
        projMoneyArr[0] = 5
        projMoneyArr[3] = 200
        default.add(projMoneyArr)

        val caffArr = IntArray(4)
        caffArr[0] = 6
        caffArr[3] = 200
        default.add(caffArr)
//
//
//        for(i in 0..6){
//            println(test[i][0])
//        }

        gamestateData = GamestateData(default,linesOfCode,linesPerSecond,linesPerClick,contract,team,100,20,500,200)
    }


//
//
//    fun setDefaultUpgrades(){
//        for (i in 0..6){
//            println("I: $i")
//        }
//
//    }



    /**
     * Funkcja oblicza liczbę linii kodu na sekunde na podstawie wykupionego zespołu i dostępnych ulepszeń
     */
    private fun calculateLPS() {
        val lpsUpgradesValue = gamestateData.upgrades[0][2]
        val teamLpsModifier = gamestateData.upgrades[3][2]


        var tempLps = 0
        tempLps+= lpsUpgradesValue

        val progList = gamestateData.team.team_getTeamMembers()
        if(progList.size >0){
            println("SIZE: "+progList.size)
            for(i in 0 until progList.size){
//                println(prog_list[i])
//                println("I: $i")
                var lpsValue = progList[i].getLinesPerSecond().toFloat()
                lpsValue += lpsValue*(teamLpsModifier/100)
                tempLps += lpsValue.toInt()
            }

        }
        tempLps += gamestateData.linesPerSecondDefault
//        prog_list[0].getLinesPerSecond()

        gamestateData.linesPerSecond = tempLps
    }

    /**
     * Funkcja oblicza liczbę linii kodu na kliknięcie na podstawie dostępnych ulepszeń
     */
    private fun calculateLPC(){
        val total = gamestateData.linesPerClickDefault + gamestateData.upgrades[1][2]
        gamestateData.linesPerClick = total
    }

    /**
     * Funkcja zmienia wartości linni kodu za ulepszenia
     * @param upgrades_value tablica wartości linii kodu za ulepszenia
     *
     */
//    fun changeUpgradesValues(upgrades_value: IntArray){
//        gamestateData.upgradesValue = upgrades_value
//        calculateLPS()
//    }

    /**
     * Funkcja zmienia tablicę z ulepszeniami
     * @param upgrades nowa tablica ulepszeń
     *
     */
//    fun changeUpgrades(upgrades: BooleanArray) {
//        gamestateData.upgrades = upgrades
//        calculateLPS()
//    }


    /**
     * Dodaje bierny przychód do całkowitej ilości
     */
    fun addPassiveIncome(){
        updateTotal(gamestateData.linesPerSecond)
    }

    /**
     * Główna funkcja, gdy wywołana będzie aktualizowała całkowitą ilość linii kodu
     * @param times_clicked ilość kliknięć w danym interwale czasowym
     *
     */
    fun setGamestate(times_clicked: Int) {
        calculateLPC()
        this.timesClicked = times_clicked
        updateTotal(calculateAmount(false))
    }

    /**
     * Funkcja koduje dane do formatu JSON
     * @return String w formacie JSON
     *
     */
    fun toJson() :String {
        return Json.encodeToString(gamestateData)
    }

    /**
     * Funkcja dekoduje JSON na format klasy GamestateData
     *
     */
    fun fromJson(string: String){
        val obj = Json.decodeFromString<GamestateData>(string)
        this.gamestateData=obj
//        println(gamestateData.linesPerSecond)
        calculateLPS()
        calculateLPC()
//        println(gamestateData.linesPerSecond)
    }


    fun getContractCost(contractDifficulty: Int):Int{
        calculateLPC()
        return when(contractDifficulty){
            0 -> contractTime*gamestateData.contracts.easyMode*gamestateData.linesPerClick
            1 -> contractTime*gamestateData.contracts.mediumMode*gamestateData.linesPerClick
            2 -> contractTime*gamestateData.contracts.hardMode*gamestateData.linesPerClick
            else -> 0
        }
    }

    /**
     * Zaczyna aktywność kontraktu
     * @param contractDifficulty wybrany poziom trudności kontraktu przekazywany przez główny wątek
     * @return wartość kontraktu
     */
    fun startContract(contractDifficulty: Int):Int{
        calculateLPC()
        this.contractDifficulty = contractDifficulty
        return when(contractDifficulty){
            0 ->{
                deductCode(contractTime*gamestateData.contracts.easyMode*gamestateData.linesPerClick)
                contractTime * gamestateData.contracts.easyMode
            }
            1 ->{
                deductCode(contractTime*gamestateData.contracts.mediumMode*gamestateData.linesPerClick)
                contractTime * gamestateData.contracts.mediumMode
            }
            2 ->{
                deductCode(contractTime*gamestateData.contracts.hardMode*gamestateData.linesPerClick)
                contractTime * gamestateData.contracts.hardMode
            }

            else -> 0
        }
    }

    /**
     * Wywołuje liczenie wartości w zależności od powodzenia kontraktu
     * @param result rezultat kontraktu (powodzenie/porażka)
     * @param times_clicked ilość kliknięć, używana gdy kontrakt sie nie udał
     */
    fun endContract(result: Boolean){
        if(result){
            gamestateData.contracts.timesSucceeded++
//            updateTotal(calculateAmount(true))
            when(contractDifficulty){
                0 -> calculateMoney(150)
                1 -> calculateMoney(200)
                2 -> calculateMoney(250)
            }
            gamestateData.contracts.checkDiff()
        }
        else{
            gamestateData.contracts.timesFailed++
//            updateTotal(calculateAmount(false))
            gamestateData.contracts.checkDiff()
        }
    }

    /**
     * Oblicza ile linii kodu trzeba dodac do total
     * @param contract czy kontrakt był sukcesem czy nie
     * @return ilość linii kodu które trzeba dodać to tablicy
     *
     */
    private fun calculateAmount(contract: Boolean) : Int{
        return if(contract){
            when(contractDifficulty){
                0 -> (contractTime * gamestateData.contracts.easyMode * gamestateData.linesPerClick * (gamestateData.contracts.easyMode.toFloat()/10)).toInt()
                1 -> (contractTime * gamestateData.contracts.mediumMode * gamestateData.linesPerClick * (gamestateData.contracts.mediumMode/10.toFloat())).toInt()
                2 -> (contractTime * gamestateData.contracts.hardMode * gamestateData.linesPerClick * (gamestateData.contracts.hardMode.toFloat()/10)).toInt()
                else -> 0
            }
        }
        else{
            val amount = timesClicked * gamestateData.linesPerClick
            amount
        }
    }

    /**
     * Funkcja aktualizuje tablicę linesOfCode i ją porządkuje
     * @param amount ilość linii kodu do dodania
     */
    private fun updateTotal(amount: Int) {

        var temp = amount
        var iterator = 1
        val ten = 10
        while (temp > 0) {

            var modulo = ten.toDouble().pow(iterator).toInt()
            var result = temp % modulo
            while(result>9){
                modulo/=10
                result%= modulo
            }
            gamestateData.linesOfCode[iterator - 1] += result
            temp /= 10
            iterator++
        }
        //println("TEMP: $temp")
        while (true) {
            var fixed = true
            for (j in gamestateData.linesOfCode) {
                if (j > 9) {
                    fixed = false
                    break
                }
            }
            if (fixed) break

            for (i in gamestateData.linesOfCode.indices) {
                if (gamestateData.linesOfCode[i] > 9) {
                    temp = gamestateData.linesOfCode[i] / 10
                    gamestateData.linesOfCode[i] %= 10
                    if(i+1!= 10) {
                        gamestateData.linesOfCode[i + 1] += temp
                    }
                }
            }
        }
    }

    /**
     * Funkcja liczy całkowitą ilość linii kodu
     * @return ilość linii kodu
     *
     */
    fun getGamestate(): Int {
        var temp = 0
        val ten= 10
        for (i in gamestateData.linesOfCode.indices) {
            temp += gamestateData.linesOfCode[i] * ten.toDouble().pow(i).toInt()
        }
        return temp
    }


    /**
     * Funkcja wywoływana przy kupowaniu ulepszenia, zwiększa wartość ulepszenia i tier ulepszenia
     * @param upgradeType numer rodzaju ulepszenia
     */

    private fun updateUpgrades(upgradeType: Int){

        when(upgradeType){

            /**
             * @param upgradeType = 0
             * LPS
             */
            0->{
                var currUpgradeValue = gamestateData.upgrades[0][2]
                var increase = 0
                if(currUpgradeValue==0){
                    currUpgradeValue+=10
                }
                else{
                    increase = (currUpgradeValue * 0.20).toInt()
                }
                currUpgradeValue += increase
                gamestateData.upgrades[0][1] +=1
                gamestateData.upgrades[0][2] = currUpgradeValue
                gamestateData.upgrades[0][3] = (gamestateData.upgrades[0][3] *1.5).toInt()
                calculateLPS()

            }

            /**
             * @param upgradeType = 1
             * LPC
             */
            1->{
                var currUpgradeValue = gamestateData.upgrades[1][2]
                var increase = 0
                if(currUpgradeValue==0){
                    currUpgradeValue+=10
                }
                else{
                    increase = (currUpgradeValue * 0.20).toInt()
                }
                currUpgradeValue += increase
                gamestateData.upgrades[1][1] +=1
                gamestateData.upgrades[1][2] = currUpgradeValue
                gamestateData.upgrades[1][3] = (gamestateData.upgrades[1][3] *1.5).toInt()
                calculateLPC()
            }

            /**
             * @param upgradeType = 2
             * CRITICAL CHANCE
             */
            2->{
                var increase = 0
                var currUpgradeValue = gamestateData.upgrades[2][2]
                if(currUpgradeValue==0){
                    currUpgradeValue+=10
                }
                else{
                    increase = 5
                }

                currUpgradeValue += increase
                gamestateData.upgrades[2][1] +=1
                gamestateData.upgrades[2][2] = currUpgradeValue
                gamestateData.upgrades[2][3] = (gamestateData.upgrades[2][3] *1.5).toInt()
            }

            /**
             * @param upgradeType = 3
             * TEAM LPS MODIFIER
             */
            3->{
                var increase = 0
                var currUpgradeValue = gamestateData.upgrades[3][2]
                if(currUpgradeValue==0){
                    currUpgradeValue+=10
                }
                else{
                    increase = 5
                }
                currUpgradeValue += increase
                gamestateData.upgrades[3][1] +=1
                gamestateData.upgrades[3][2] = currUpgradeValue
                gamestateData.upgrades[3][3] = (gamestateData.upgrades[3][3] *1.5).toInt()
            }

            /**
             *  @param upgradeType = 4
             * PROGRAMMER SPACE
             */
            4->{
                var increase = 0
                var currUpgradeValue = gamestateData.upgrades[4][2]
                if(currUpgradeValue==0){
                    currUpgradeValue+=2
                }
                else{
                    increase = 1
                }
                currUpgradeValue += increase
                gamestateData.upgrades[4][1] +=1
                gamestateData.upgrades[4][2] = currUpgradeValue
                gamestateData.upgrades[4][3] = (gamestateData.upgrades[4][3] *1.5).toInt()
            }

            /**
             * @param upgradeType = 5
             * PROJECTS MONEY MODIFIER
             */
            5->{
                var increase = 0
                var currUpgradeValue = gamestateData.upgrades[5][2]
                if(currUpgradeValue==0){
                    currUpgradeValue+=10
                }
                else{
                    increase = 5
                }
                currUpgradeValue += increase
                gamestateData.upgrades[5][1] +=1
                gamestateData.upgrades[5][2] = currUpgradeValue
                gamestateData.upgrades[5][3] = (gamestateData.upgrades[5][3] *1.5).toInt()
            }

            /**
             * @param upgradeType = 6
             * CAFFEINE GAIN MODIFIER
             */
            6->{
                var increase = 0
                var currUpgradeValue = gamestateData.upgrades[6][2]
                if(currUpgradeValue==0){
                    currUpgradeValue+=10
                }
                else{
                    increase = 5
                }
                currUpgradeValue += increase
                gamestateData.upgrades[6][1] +=1
                gamestateData.upgrades[6][2] = currUpgradeValue
                gamestateData.upgrades[6][3] = (gamestateData.upgrades[6][3] *1.5).toInt()
            }
        }
    }

    /**
     * Funkcja zwraca szansę na kliknięcie krytyczne
     */
    fun getCritChance(): Float {
        val critChance = gamestateData.upgrades[2][2].toFloat()
        return critChance/100
    }


    /**
     * Funkcja nakłada modifikator na ilość pieniędzy którą zyskaliśmy
     */
    fun calculateMoney(amount: Int){
        val modifier = gamestateData.upgrades[5][2].toFloat() / 100
        gamestateData.money += amount + (amount * modifier).toInt()
    }

    /**
     * Funkcja nakłada modifikator na ilość kofeiny którą zyskaliśmy
     */
    fun calculateCaffeine(amount: Int){
        val modifier = gamestateData.upgrades[6][2].toFloat() / 100
        gamestateData.caffeine += amount + (amount * modifier).toInt()
    }

    /**
     * Funkcja sprawdza czy mamy wystarczająco dużo miejsca żeby zatrudnić nowego programistę
     */
    private fun checkProgrammerSpace(): Boolean {
        val currentAmountOfProgrammers = gamestateData.team.team_getTeamMembers().size
        return currentAmountOfProgrammers < gamestateData.upgrades[4][2]
    }

    private fun checkIfEnoughResourceToBuy(resource: String, amount: Int):Boolean{
        when(resource){
            "money"->{
                if(gamestateData.money>=amount) return true
                println("Not enough money")
            }
            "caffeine"->{
                if(gamestateData.caffeine>=amount) return true
                println("Not enough caffeine")
            }
            "code"->{
                if(getGamestate()>=amount) return true
                else println("Not enough lines of code")
            }
        }
        return false
    }

    private fun deductMoney(amount: Int){
        gamestateData.money-=amount
    }

    private fun deductCaffeine(amount: Int){
        gamestateData.caffeine-=amount
    }

    private fun deductCode(amount: Int){
        var temp = getGamestate() - amount
        val linesOfCode = IntArray(10)
        var iterator = 1
        val ten = 10
        while (temp > 0) {

            var modulo = ten.toDouble().pow(iterator).toInt()
            var result = temp % modulo
            while(result>9){
                modulo/=10
                result%= modulo
            }
            linesOfCode[iterator - 1] += result
            temp /= 10
            iterator++
        }
        println("TEMP: $temp")
        while (true) {
            var fixed = true
            for (j in linesOfCode) {
                if (j > 9) {
                    fixed = false
                    break
                }
            }
            if (fixed) break

            for (i in linesOfCode.indices) {
                if (linesOfCode[i] > 9) {
                    temp = linesOfCode[i] / 10
                    linesOfCode[i] %= 10
                    if(i+1!= 10) {
                        linesOfCode[i + 1] += temp
                    }
                }
            }
        }
        gamestateData.linesOfCode = linesOfCode

    }

    fun purchaseUpgradeFromStore(upgradeType: Int,moneyAmount: Int):Boolean{
        if(checkIfEnoughResourceToBuy("money",moneyAmount)){
            deductMoney(moneyAmount)
            updateUpgrades(upgradeType)
            return true
        }
        return false
    }

    fun hireProgrammer(programmer: Programmer):Boolean{
        if(checkIfEnoughResourceToBuy("money",programmer.getPrice())&&checkProgrammerSpace()){
            deductMoney(programmer.getPrice())
            gamestateData.team.team_addMember(programmer)
            calculateLPS()
            return true
        }
        return false

    }

    fun buyCaffeine(amount: Int, moneyAmount: Int):Boolean{
        if(checkIfEnoughResourceToBuy("money",moneyAmount)){
            deductMoney(moneyAmount)
            calculateCaffeine(amount)
            return true
        }
        return false
    }

    fun buyContract(difficulty : Int) : Boolean {
        return checkIfEnoughResourceToBuy("code", getContractCost(difficulty))
    }


    fun getUpgradesNames(): Array<String> {
        return arrayOf(
            "Uczenie maszynowe\nLinijki na sek.",
            "Podswietlana klawiatura\nModyfikator linijek na klik.",
            "Zmysl developera\nSzansa na krytyczne kliknięcie.",
            "Korpo szkolenia\nModyfikator linijek na sek. Zespolu",
            "Rozbudowa biura\nMiejsce na dodatkowego programiste w Zespole",
            "Udzialy na gieldzie\nModyfikator kasy za projekty",
            "Dealer ziaren\nModyfikator kupowanej kofeiny"
        )
    }

    fun getUpgradeCost(upgradeType: Int):Int{
        return gamestateData.upgrades[upgradeType][3]
    }

    fun getUpgradeValue(upgradeType: Int):Int{
        return gamestateData.upgrades[upgradeType][2]
    }

    fun getUpgradeTier(upgradeType: Int):Int{
        return gamestateData.upgrades[upgradeType][1]
    }





}




/**
 * Main stworzony do testowania poprawności działania funkcji
 *
//  */
// <<<<<<< kacper_mainloop
// fun main() {

//     val gamestate = Gamestate()
//     gamestate.setDefaultValues()

//     for(i in 0..9){
//         gamestate.setGamestate(11)
//         println(gamestate.getGamestate())
//         println("----------------------------")

//     }

// //    val upgrades: BooleanArray = booleanArrayOf(false,false,false,false,false,false,false,true)
// //    gamestate.changeUpgrades(upgrades)
//     for(i in 0..10){
//         gamestate.setGamestate(11)
//         println(gamestate.getGamestate())
//     }
//     println("--------------------------")
//     println(gamestate.toJson())
//     println(gamestate.gamestateData.linesPerClick)
// //    gamestate.startContract(2)
// //    gamestate.endContract(false,750)
// //    gamestate.endContract(false,750)
// //    gamestate.endContract(false,750)
// //    gamestate.endContract(false,750)
//     println(gamestate.getGamestate())
//     val js = gamestate.toJson()
//     gamestate.fromJson(js)
//     println(gamestate.toJson())
//     println(gamestate.gamestateData.contracts.easyMode)
//     gamestate.calculateLPS()
//     println(gamestate.getGamestate())
//     gamestate.updateTotal(200)
//     println(gamestate.getGamestate())
//     gamestate.updateTotal(200)
//     gamestate.calculateCaffeine(500)
//     gamestate.calculateMoney(700)
//     println(gamestate.getGamestate())
//     println(gamestate.toJson())
//     println(gamestate.purchaseUpgradeFromStore(1,gamestate.gamestateData.upgrades[1][3]))
//     println(gamestate.toJson())
//     println(gamestate.purchaseUpgradeFromStore(1,gamestate.gamestateData.upgrades[1][3]))
//     println(gamestate.toJson())
//     println(gamestate.purchaseUpgradeFromStore(1,gamestate.gamestateData.upgrades[1][3]))
//     println(gamestate.toJson())
//     println(gamestate.purchaseUpgradeFromStore(1,gamestate.gamestateData.upgrades[1][3]))
//     println(gamestate.toJson())
// //    println(gamestate.purchaseUpgradeFromStore(0,100,100,100))
// //    println(gamestate.toJson())
// ////    println(gamestate.purchaseUpgradeFromStore(2,100,100,100))
//     gamestate.calculateCaffeine(500)
//     gamestate.calculateMoney(700)
// //    println(gamestate.toJson())
// //    println(gamestate.purchaseUpgradeFromStore(3,100,100,100))
// =======
// //fun main() {
// //
// //    val gamestate = Gamestate()
// //    gamestate.setDefaultValues()
// //
// //    for(i in 0..9){
// //        gamestate.setGamestate(20, 11)
// //        println(gamestate.getGamestate())
// //        println("----------------------------")
// //
// //    }
// //
// ////    val upgrades: BooleanArray = booleanArrayOf(false,false,false,false,false,false,false,true)
// ////    gamestate.changeUpgrades(upgrades)
// //    for(i in 0..10){
// //        gamestate.setGamestate(20, 11)
// //        println(gamestate.getGamestate())
// //    }
// //    println("--------------------------")
// >>>>>>> main
// //    println(gamestate.toJson())
// //    println(gamestate.gamestateData.linesPerClick)
// ////    gamestate.startContract(2)
// ////    gamestate.endContract(false,750)
// ////    gamestate.endContract(false,750)
// ////    gamestate.endContract(false,750)
// ////    gamestate.endContract(false,750)
// //    println(gamestate.getGamestate())
// //    val js = gamestate.toJson()
// //    gamestate.fromJson(js)
// //    println(gamestate.toJson())
// //    println(gamestate.gamestateData.contracts.easyMode)
// //    gamestate.calculateLPS()
// //    println(gamestate.getGamestate())
// //    gamestate.updateTotal(200)
// //    println(gamestate.getGamestate())
// //    gamestate.updateTotal(200)
// //    gamestate.calculateCaffeine(500)
// //    gamestate.calculateMoney(700)
// //    println(gamestate.getGamestate())
// //    println(gamestate.toJson())
// //    println(gamestate.purchaseUpgradeFromStore(1,gamestate.gamestateData.upgrades[1][3]))
// //    println(gamestate.toJson())
// //    println(gamestate.purchaseUpgradeFromStore(1,gamestate.gamestateData.upgrades[1][3]))
// //    println(gamestate.toJson())
// //    println(gamestate.purchaseUpgradeFromStore(1,gamestate.gamestateData.upgrades[1][3]))
// //    println(gamestate.toJson())
// //    println(gamestate.purchaseUpgradeFromStore(1,gamestate.gamestateData.upgrades[1][3]))
// //    println(gamestate.toJson())
// ////    println(gamestate.purchaseUpgradeFromStore(0,100,100,100))
// ////    println(gamestate.toJson())
// //////    println(gamestate.purchaseUpgradeFromStore(2,100,100,100))
// //    gamestate.calculateCaffeine(500)
// //    gamestate.calculateMoney(700)
// ////    println(gamestate.toJson())
// ////    println(gamestate.purchaseUpgradeFromStore(3,100,100,100))
// ////    println(gamestate.toJson())
// ////    println(gamestate.purchaseUpgradeFromStore(4,100,100,100))
// ////    println(gamestate.toJson())
// ////    println(gamestate.purchaseUpgradeFromStore(5,100,100,100))
// ////    gamestate.calculateCaffeine(500)
// ////    println(gamestate.toJson())
// ////    println(gamestate.purchaseUpgradeFromStore(6,100,100,100))
// ////    println(gamestate.toJson())
// ////    println(gamestate.purchaseUpgradeFromStore(1,100,100,100))
// ////    println(gamestate.toJson())
// ////    println(gamestate.getGamestate())
// ////    gamestate.calculateCaffeine(500)
// ////    println(gamestate.toJson())
// ////    gamestate.calculateCaffeine(500)
// ////    gamestate.deductCode(200)
// ////    println(gamestate.getGamestate())
// ////
// ////    gamestate.setDefaultUpgrades()
// //
// ////    println(gamestate.gamestateData)
// ////
// ////
// ////
// ////    var string = gamestate.toJson()
// ////    println(string)
// ////    string="""{"upgrades":[false,false,true,true,false,false,false,true],"upgradesValue":[30,80,120,200,450,670,900,1340],"linesOfCode":[0,6,3,0,2,0,0,0,0,0],"linesPerSecond":1340,"linesPerClick":20}"""
// ////    gamestate.fromJson(string)
// ////    println(gamestate.gamestateData)
// ////    println(gamestate.toJson())
// ////    println(gamestate.gamestateData.contracts)
// //////    gamestate.gamestateData.contracts.increaseDifficulty()
// //////    println(gamestate.gamestateData.contracts.easyMode)
// //////    println(gamestate.gamestateData.contracts.mediumMode)
// //////    println(gamestate.gamestateData.contracts.hardMode)
// //////    println(gamestate.gamestateData.contracts.timesFailed)
// //////    println(gamestate.gamestateData.contracts.timesSucceeded)
// //////
// //////    println(gamestate.gamestateData.contracts.easyMode)
// //////    println(gamestate.gamestateData.contracts.mediumMode)
// //////    println(gamestate.gamestateData.contracts.hardMode)
// //////    println(gamestate.gamestateData.contracts.timesFailed)
// //////    println(gamestate.gamestateData.contracts.timesSucceeded)
// ////
// ////    gamestate.gamestateData.team.team_addMember(Programmer(1))
// ////    gamestate.gamestateData.team.team_addMember(Programmer(2))
// //    println(gamestate.toJson())
// //}