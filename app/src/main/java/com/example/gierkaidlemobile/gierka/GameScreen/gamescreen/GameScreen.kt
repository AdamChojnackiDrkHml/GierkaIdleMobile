package GameScreen.gamescreen

import GameScreen.IdleGame
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import concurrency.GameStateMonitor
import concurrency.GameStateMonitor.incrementClickCount
import concurrency.TaskManager
import resources.NotEnoughResourceException
import ktx.actors.onClick
import ktx.app.clearScreen
import ktx.scene2d.*
import utils.shop.Programmer
import utils.shop.Shop

val screenWidth = Gdx.graphics.width
val screenHeight = Gdx.graphics.height

/**
 * Actual game screen, shows after successful user login
 * @param game Instance of IdleGame class
 */
class GameScreen(private val game: IdleGame) : Screen {
    private val shop = Shop().apply { generateProgrammerOffer(3, 1) }
    private lateinit var upgradeNames : Array<String>
    private lateinit var upgrades : Triple<IntArray, IntArray, IntArray>
    private lateinit var caffeineNames : Array<String>
    private lateinit var caffeineOffers : Pair<IntArray, IntArray>
    private lateinit var programmers : MutableList<Programmer>
    private lateinit var teamMembers : MutableList<Programmer>
    private val laptopWidth = 600
    private val laptopHeight = 600
    private val textureSize = 2000
    private val verticalPadding = 100f
    private val uiPadding = 10f
    private val scalingRatio = laptopWidth.toFloat() / textureSize.toFloat()
    private var stage = Stage()
    private var contractStage = Stage()
    private var shapeRenderer = ShapeRenderer()
    private var batch = game.batch
    private var camera = game.camera
    private val laptopTexture = scaleTexture(Gdx.files.internal("Images/Textures/Computers/T_LaptopGrey.png"), scalingRatio)
    private val laptopBackgroundTexture = scaleTexture(Gdx.files.internal("Images/Textures/ComputerWallpapers/T_BackgroundDefaultBinary.png"), scalingRatio)
    private val contractBackgroundTexture = scaleTexture(Gdx.files.internal("Images/Textures/ComputerWallpapers/T_BackgroundContractCursors.png"), scalingRatio)
    private val lineTexture = scaleTexture(Gdx.files.internal("Images/Textures/Icons/T_IconCode.png"), 0.07f)
    private val cashTexture = scaleTexture(Gdx.files.internal("Images/Textures/Icons/T_IconCash.png"), 0.07f)
    private val coffeeTexture = scaleTexture(Gdx.files.internal("Images/Textures/Icons/T_IconCoffee.png"), 0.07f)
    private val bananaTexture = scaleTexture(Gdx.files.internal("Images/Textures/Logos/T_LogoBanana.png"), 0.1f)
    private val doodleTexture = scaleTexture(Gdx.files.internal("Images/Textures/Logos/T_LogoDoodle.png"), 0.1f)
    private val dyTexture = scaleTexture(Gdx.files.internal("Images/Textures/Logos/T_LogoDy.png"), 0.1f)
    private val macroHardTexture = scaleTexture(Gdx.files.internal("Images/Textures/Logos/T_LogoMacroHard.png"), 0.1f)
    private val yeskiaTexture = scaleTexture(Gdx.files.internal("Images/Textures/Logos/T_LogoYeskia.png"), 0.1f)
    private val programmerTextureFilenames = arrayOf("Images/Textures/Programmers/T_Programmer01.png", "Images/Textures/Programmers/T_Programmer02.png", "Images/Textures/Programmers/T_Programmer03.png")
    private val logoTextures = arrayOf(bananaTexture, doodleTexture, dyTexture, macroHardTexture, yeskiaTexture)
    private val contractNames = arrayOf("Banana", "Doodle", "dy", "MacroHard", "Yeskia")
    private val difficulties = arrayOf("easy", "medium", "hard")
    private var laptopRect : Rectangle
    private val colourOfCode = Color.valueOf("#7200D2")
    private val colourOfCash = Color.valueOf("#D5D545")
    private val colourOfCoffee = Color.valueOf("#641D00")
    private var teamIncome = 0
    private var timeOfPurchase : Long = 0
    private var contractProgress : Float = 30f
    private val contractLogoX : Float = screenWidth / 2f - 155f
    private val contractLogoY : Float = screenHeight / 2f + 70f
    private var contractBarHeight : Float = 25f
    private var contractBarWidth : Float = 200f
    private lateinit var currentContract : Texture
    private var contractRunning = false
    private var contractCosts = arrayOf(1000, 1000, 1000)
    private var contractTime : Long = 0L

    /**
     *
     * Graphical elements - scene2d widgets
     *
     */

    private var lineLabel: Label = scene2d.label(text = currentLines.toString()) {
        color = colourOfCode
    }
    private var cashLabel: Label = scene2d.label(text = currentCash.toString()) {
        color = colourOfCash
    }
    private var caffeineLabel: Label = scene2d.label(text = currentCaffeine.toString()) {
        color = colourOfCoffee
    }
    /**
     * Displays current score (lines of code, cash and caffeine)
     */
    private val scoreTable = scene2d.table {
        width = 300f
        height = 100f
        x = (screenWidth - width) / 2
        y = screenHeight - height
        table {
            image(lineTexture)
            add(lineLabel)
        }
        row()
        table {
            add(cashLabel)
            image(cashTexture)
        }
        row()
        table {
            add(caffeineLabel)
            image(coffeeTexture)
        }
    }

    /**
     * Opens contract menu
     */
    private var contractButton = scene2d.textButton(text = "Contracts") {
        width = 200f
        height = 50f
        x = 0f + uiPadding
        y = screenHeight - height - uiPadding
        color = Color.SLATE
        onClick { showContractMenu() }
    }
    private val contractOfferTable = scene2d.table {
        background = getBackgroundColorTRD(Color.valueOf("#401010"))
    }
    private val contractScrollPane = scene2d.scrollPane {
        addActor(contractOfferTable)
        addListener(object : InputListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                stage.scrollFocus = this@scrollPane
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                stage.scrollFocus = null
            }
        })
    }
    /**
     * Contains contract menu
     */
    private val contractTable = scene2d.table {
        isVisible = false
        width = 300f
        height = screenHeight / 2 - 50f - uiPadding
        x = uiPadding
        y = screenHeight - height - 50f - uiPadding
        add(contractScrollPane).width(300f).height(screenHeight / 2f - 50f - uiPadding)
    }

    /**
     * Opens shop menu
     */
    private var shopButton = scene2d.textButton(text = "Shop") {
        width = 200f
        height = 50f
        x = uiPadding
        y = uiPadding
        color = Color.SLATE
        onClick { showShopMenu() }
    }
    private val shopOfferTable = scene2d.table {}
    private val shopScrollPane = scene2d.scrollPane {
        addActor(shopOfferTable)
        addListener(object : InputListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                stage.scrollFocus = this@scrollPane
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                stage.scrollFocus = null
            }
        })
    }
    /**
     * Switches shop menu to upgrade section
     */
    private var upgradeButton = scene2d.textButton(text = "Upgrades") {
        color = Color.TEAL
        onClick { showUpgrades() }
    }
    /**
     * Switches shop menu to programmer section
     */
    private var programmerButton = scene2d.textButton(text = "Programmers") {
        color = Color.CYAN
        onClick { showProgrammers() }
    }
    /**
     * Switches shop menu to caffeine section
     */
    private var caffeineButton = scene2d.textButton(text = "Caffeine") {
        color = Color.SKY
        onClick { showCaffeineOffers() }
    }
    /**
     * Contains shop menu
     */
    private val shopTable = scene2d.table {
        isVisible = false
        width = 300f
        height = screenHeight / 2 - 50f - uiPadding
        x = uiPadding
        y = screenHeight / 2f - height
        add(shopScrollPane).width(300f).height(screenHeight / 2f - 50f - uiPadding - 30f)
        row()
        table {
            add(upgradeButton).width(90f).height(30f)
            add(programmerButton).width(120f).height(30f)
            add(caffeineButton).width(90f).height(30f)
        }
    }

    /**
     * Opens team menu
     */
    private var teamButton = scene2d.textButton(text = "Team") {
        width = 200f
        height = 50f
        x = screenWidth - width - uiPadding
        y = screenHeight - height - uiPadding
        color = Color.SLATE
        onClick { showTeamMenu() }
    }
    private val ownedTeamTable = scene2d.table {}
    private val teamScrollPane = scene2d.scrollPane {
        addActor(ownedTeamTable)
        addListener(object : InputListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                stage.scrollFocus = this@scrollPane
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                stage.scrollFocus = null
            }
        })
    }
    private var teamIncomeLabel = scene2d.label("+$teamIncome/s") {
        color = colourOfCode
    }
    /**
     * Contains team menu
     */
    private val teamTable = scene2d.table {
        isVisible = false
        width = 300f
        height = screenHeight / 2 - 50f - uiPadding
        x = screenWidth - width - uiPadding
        y = screenHeight - height - 50f - uiPadding
        table {
            add(teamIncomeLabel).width(300f).height(30f)
            background = getBackgroundColorTRD(Color.LIGHT_GRAY)
        }
        row()
        add(teamScrollPane).width(300f).height(screenHeight / 2f - 50f - uiPadding - 30f)
    }

    private val upgradeLabel = scene2d.label(text = "") {
        color = Color.valueOf("#205050")
    }
    private val upgradeTable = scene2d.table {
        width = 300f
        height = 50f
        x = (screenWidth  - width) / 2f
        y = screenHeight / 2 + 3.5f * height
        add(upgradeLabel)
    }

    private var errorLabel = scene2d.label(text = "") {
        color = Color.RED
    }
    /**
     * Displays error messages
     */
    private val errorTable = scene2d.table {
        width = 300f
        height = 50f
        x = (screenWidth - width) / 2
        y = 0f
        add(errorLabel)
    }

    private var contractStatusLabel = scene2d.label(text = "") {
        color = Color.FOREST
    }
    /**
     * Info after finishing contract
     */
    private val contractStatusTable = scene2d.table {
        width = 300f
        height = 100f
        x = (screenWidth - width) / 2
        y = 0f
        add(contractStatusLabel)
    }


    /**
     * Contains the background image of the game screen
     */
    private val backgroundTable = scene2d.table {
        background(TextureRegionDrawable(TextureRegion(game.screenBackgroundTexture)))
        setFillParent(true)
    }

    private val contractDifficultyLabel = scene2d.label(text = "Difficulty: easy") {
        color = Color.SKY
    }
    private val contractNameLabel = scene2d.label(text = "Contract: Doodle") {
        color = Color.SKY
    }
    private val contractCompletionLabel = scene2d.label(text = "0%") {
        color = Color.FOREST
    }
    private val contractTimeLabel = scene2d.label(text = "00:000") {
        color = Color.GOLDENROD
    }
    private var contractProgressBar = scene2d.progressBar {
        color = Color.SKY
    }
    /**
     * Contains status of currently selected contract
     */
    private val currentContractTable = scene2d.table {
        isVisible = false
        x = contractLogoX + 225f
        y = contractLogoY + 30f
        table {
            add(contractNameLabel)
            row()
            add(contractDifficultyLabel)
        }
        row()
        table {
            add(contractCompletionLabel)
            row()
            add(contractTimeLabel)
        }
    }

    /**
     * Exits the game on click
     */
    private val exitButton = scene2d.textButton(text = "Exit") {
        width = 100f
        height = 50f
        x = screenWidth - width - uiPadding
        y = uiPadding
        color = Color.DARK_GRAY
        onClick { exitGame() }
    }

    /**
     *
     * Methods
     *
     */

    init {
        stage.addActor(backgroundTable)
        stage.addActor(scoreTable)
        stage.addActor(errorTable)
        stage.addActor(contractStatusTable)
        stage.addActor(upgradeTable)
        stage.addActor(contractButton)
        stage.addActor(contractTable)
        stage.addActor(shopButton)
        stage.addActor(shopTable)
        stage.addActor(teamButton)
        stage.addActor(teamTable)
        stage.addActor(exitButton)
        contractStage.addActor(currentContractTable)
        laptopRect = getLaptopRectangle()
    }

    fun enter() {
        upgradeNames = shop.getUpgradeNamesList()
        upgrades = shop.showUpgradesOffer()
        caffeineNames = shop.getCaffeineNamesList()
        caffeineOffers = shop.showCaffeineOffer()
        programmers = shop.showProgrammerOffer()

        for(i in programmers.indices) {
            val filename = programmerTextureFilenames[(programmerTextureFilenames.indices).random()]
            programmers[i].setImageFilename(filename)
        }

        showProgrammers()
        showContracts()
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        contractProgress = contractProgressBar.value / contractProgressBar.maxValue * 100
        clearScreen(0f, 0f, 0f, 0f)
        stage.act()
        stage.draw()
        camera.update()
        batch.projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined

        updateDisplay()

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            val position = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            if (laptopRect.x <= position.x && position.x <= laptopRect.x + laptopWidth) {
                if (laptopRect.y + verticalPadding <= position.y && position.y <= laptopRect.y + laptopHeight - verticalPadding) {
                    incrementClickCount()
                }
            }
        }

        batch.begin()
        batch.draw(laptopTexture, laptopRect.x, laptopRect.y)
        if(contractRunning) {
            contractCompletionLabel.setText(String.format("%.2f", contractProgress) + "%")
            batch.draw(contractBackgroundTexture, laptopRect.x, laptopRect.y)
            batch.draw(currentContract, contractLogoX, contractLogoY)
        }
        else batch.draw(laptopBackgroundTexture, laptopRect.x, laptopRect.y)
        batch.end()

        if(contractRunning) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.GOLDENROD
            shapeRenderer.rect((screenWidth - contractBarWidth) / 2, screenHeight / 2 - 30f, contractBarWidth * contractProgress / 100, contractBarHeight)
            shapeRenderer.color = Color.BLACK
            shapeRenderer.rect((screenWidth - contractBarWidth) / 2 + contractBarWidth * contractProgress / 100, screenHeight / 2 - 30f, contractBarWidth * (1 - contractProgress / 100), contractBarHeight)
            shapeRenderer.end()
        }

        if(!contractRunning) {
            currentContractTable.isVisible = false
        }

        if(contractTime > 0L && System.currentTimeMillis() - contractTime <= 30000L) {
            contractTimeLabel.setText("")
            val total = 30000L - (System.currentTimeMillis() - contractTime)
            val sec = total / 1000L
            val millis = total - sec * 1000L
            contractTimeLabel.setText(String.format("%02d:%03d", sec, millis))
        } else if(contractTime > 0L && System.currentTimeMillis() - contractTime >= 30000L) {
            TaskManager.endContract()
            contractTime = 0L
            contractProgress = 0f
            contractRunning = false
        }

        contractStage.act()
        contractStage.draw()

        if(timeOfPurchase > 0 && System.currentTimeMillis() - timeOfPurchase >= 2000) {
            upgradeLabel.setText("")
            timeOfPurchase = 0
        }

        if(messageTime > 0 && System.currentTimeMillis() - messageTime >= 2000) {
            contractMessage = ""
            messageTime = 0
        }

        if(errorTime > 0 && System.currentTimeMillis() - errorTime >= 2000) {
            errorMessage = ""
            errorTime = 0
        }
    }

    override fun resize(width: Int, height: Int) {}

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    override fun dispose() {
        stage.dispose()
        laptopTexture.dispose()
        laptopBackgroundTexture.dispose()
        lineTexture.dispose()
        cashTexture.dispose()
        coffeeTexture.dispose()
        bananaTexture.dispose()
        doodleTexture.dispose()
        dyTexture.dispose()
        macroHardTexture.dispose()
        yeskiaTexture.dispose()
        TaskManager.finalize()
    }

    /**
     * Scales texture from fileHandle to given ratio
     * @param fileHandle FileHandle of texture to be scaled
     * @param scalingRatio Scaling proportion float
     */
    private fun scaleTexture(fileHandle: FileHandle, scalingRatio: Float) : Texture {
        val pixmap = Pixmap(fileHandle)
        val scaledPixmap = Pixmap((pixmap.width * scalingRatio).toInt(), (pixmap.height * scalingRatio).toInt(), pixmap.format)
        scaledPixmap.drawPixmap(pixmap,
            0, 0, pixmap.width, pixmap.height,
            0, 0, scaledPixmap.width, scaledPixmap.height)

        val texture = Texture(scaledPixmap)
        pixmap.dispose()
        scaledPixmap.dispose()
        return texture
    }

    /**
     * Returns a rectangle which represents laptop on screen
     */
    private fun getLaptopRectangle() : Rectangle {
        val rect = Rectangle()
        rect.width = laptopWidth.toFloat()
        rect.height = laptopHeight.toFloat()
        rect.x = (screenWidth - laptopWidth) / 2f
        rect.y = (screenHeight - laptopHeight) / 2f
        return rect
    }

    /**
     *  Returns TextureRegionDrawable of solid colour (e.g. to fill table background)
     *  @param colour Color to be set
     *  @return TextureRegionDrawable filled with solid colour to be set as ui element background
     */
    private fun getBackgroundColorTRD(colour: Color) : TextureRegionDrawable {
        val backgroundPixmap = Pixmap(1, 1, Pixmap.Format.RGB888)
        backgroundPixmap.setColor(colour)
        backgroundPixmap.fill()
        val trd = TextureRegionDrawable(TextureRegion(Texture(backgroundPixmap)))
        backgroundPixmap.dispose()
        return trd
    }

    /**
     *  Updates labels showing current amount of resources, error messages and total passive income
     *  from hired programmers
     */
    private fun updateDisplay() {
        lineLabel.setText(currentLines.toString())
        cashLabel.setText(currentCash.toString())
        caffeineLabel.setText(currentCaffeine.toString())
        errorLabel.setText(errorMessage)
        contractStatusLabel.setText(contractMessage)
        teamIncomeLabel.setText("+$teamIncome/s")
    }

    private fun showContractMenu() {
        if(!contractRunning) {
            showContracts()
            contractTable.isVisible = !contractTable.isVisible
        }
    }

    private fun showContracts() {
        contractOfferTable.clearChildren()

        for(i in 0..9) {
            contractOfferTable.add(scene2d.button {
                color = if(contractCosts[i % 3] <= currentLines) Color.valueOf("#FF5D6E") else Color.GRAY
                image(logoTextures[i % 5])
                onClick { startContract(i % 3, i % 5) }
            }).width(80f).height(80f)
            contractOfferTable.add(scene2d.table {
                color = if(contractCosts[i % 3] <= currentLines) Color.valueOf("#FF5D6E") else Color.GRAY
                label(text = "Difficulty: " + difficulties[i % 3]) {
                    setOrigin(Align.left)
                }
                row()
                label(text = "Cost: " + contractCosts[i % 3]) {
                    setOrigin(Align.left)
                }
            }).width(195f).height(75f)
            contractOfferTable.row()
        }
    }

    private fun showShopMenu() {
        shopTable.isVisible = !shopTable.isVisible
    }

    /**
     * Shows upgrade menu in shop
     */
    private fun showUpgrades() {
        val tiers = upgrades.first
        val values = upgrades.second
        val costs = upgrades.third

        shopOfferTable.clearChildren()

        for(i in upgrades.first.indices) {
            shopOfferTable.add(scene2d.button {
                color = if(costs[i] <= currentCash) Color.OLIVE else Color.GRAY
                label(text = upgradeNames[i].substring(0, upgradeNames[i].indexOf('\n')))
                row()
                label(text = upgradeNames[i].substring(upgradeNames[i].indexOf('\n') + 1)) {
                    setFontScale(0.6f)
                }
                onClick { purchaseUpgrade(i) }
            }).width(225f).height(60f)
            shopOfferTable.add(scene2d.table {
                background = if(costs[i] <= currentCash) getBackgroundColorTRD(Color.OLIVE) else getBackgroundColorTRD(Color.GRAY)
                label(text = "Tier " + tiers[i].toString()) {
                    setOrigin(Align.left)
                    setFontScale(0.7f)
                    color = Color.TEAL
                }
                row()
                label(text = "Value: " + values[i].toString()) {
                    setOrigin(Align.left)
                    setFontScale(0.7f)
                    color = colourOfCode
                }
                row()
                label(text = "Cost: " + costs[i].toString()) {
                    setOrigin(Align.left)
                    setFontScale(0.7f)
                    color = colourOfCash
                }
            }).width(50f).height(60f)
            shopOfferTable.row()
        }
    }

    /**
     * Shows caffeine menu in shop
     */
    private fun showCaffeineOffers() {
        val values = caffeineOffers.first
        val costs = caffeineOffers.second
        shopOfferTable.clearChildren()

        for(i in caffeineNames.indices) {
            shopOfferTable.add(scene2d.button {
                color = if(caffeineOffers.second[i] <= currentCash) Color.TAN else Color.GRAY
                label(text = caffeineNames[i].substring(0, caffeineNames[i].indexOf('\n')))
                row()
                label(text = caffeineNames[i].substring(caffeineNames[i].indexOf('\n') + 1)) {
                    setFontScale(0.6f)
                }
                onClick { purchaseCaffeine(i) }
            }).width(200f).height(60f)
            shopOfferTable.add(scene2d.table {
                background = if(costs[i] <= currentCash) getBackgroundColorTRD(Color.TAN) else getBackgroundColorTRD(Color.GRAY)
                label(text = "Value " + values[i].toString()) {
                    setOrigin(Align.left)
                    setFontScale(0.7f)
                    color = colourOfCode
                }
                row()
                label(text = "Cost: " + costs[i].toString()) {
                    setOrigin(Align.left)
                    setFontScale(0.7f)
                    color = colourOfCash
                }
            }).width(75f).height(60f)
            shopOfferTable.row()
        }
    }

    /**
     * Shows programmer menu in shop
     */
    private fun showProgrammers() {
        shopOfferTable.clearChildren()

        for(i in programmers.indices) {
            shopOfferTable.add(scene2d.button {
                color = if(programmers[i].getPrice() <= currentCash) Color.FOREST else Color.GRAY
                image(TextureRegionDrawable(TextureRegion(scaleTexture(Gdx.files.internal(programmers[i].getImageFilename()), 0.1f))))
                onClick { purchaseProgrammer(i) }
            }).width(125f).height(125f)
            shopOfferTable.add(scene2d.table {
                background = if(programmers[i].getPrice() <= currentCash) getBackgroundColorTRD(Color.FOREST) else getBackgroundColorTRD(Color.GRAY)
                label(text = "+" + programmers[i].getLinesPerSecond().toString() + "/s") {
                    setOrigin(Align.left)
                    color = colourOfCode
                }
                row()
                label(text = "Caffeine mod: " + programmers[i].getCaffeineMod().toString()) {
                    setOrigin(Align.left)
                    color = colourOfCoffee
                }
                row()
                label(text = "Cost: " + programmers[i].getPrice().toString()) {
                    setOrigin(Align.left)
                    color = colourOfCash
                }
            }).width(150f).height(125f)
            shopOfferTable.row()
        }
    }

    private fun showTeamMenu() {
        teamTable.isVisible = !teamTable.isVisible
        if(teamTable.isVisible) showTeamMembers()
    }

    private fun showTeamMembers() {
        teamMembers = GameStateMonitor.getTeamMembers()
        teamIncome = 0
        ownedTeamTable.clearChildren()

        for(i in teamMembers.indices) {
            teamIncome += teamMembers[i].getLinesPerSecond()
            ownedTeamTable.add(scene2d.button {
                color = Color.SKY
                image(TextureRegionDrawable(TextureRegion(scaleTexture(Gdx.files.internal(teamMembers[i].getImageFilename()), 0.1f))))
                onClick {  }
            }).width(125f).height(125f)
            ownedTeamTable.add(scene2d.table {
                background = getBackgroundColorTRD(Color.SKY)
                label(text = "+" + teamMembers[i].getLinesPerSecond().toString() + "/s") {
                    setOrigin(Align.left)
                    setFontScale(0.8f)
                    color = colourOfCode
                }
                row()
                label(text = "Caffeine: " + teamMembers[i].getCaffeineAmount().toString()) {
                    setOrigin(Align.left)
                    setFontScale(0.8f)
                    color = colourOfCoffee
                }
                row()
                label(text = "Caffeine mod: " + teamMembers[i].getCaffeineMod().toString()) {
                    setOrigin(Align.left)
                    setFontScale(0.8f)
                    color = colourOfCoffee
                }
                row()
                label(text = "Cost: " + teamMembers[i].getPrice().toString()) {
                    setOrigin(Align.left)
                    setFontScale(0.8f)
                    color = colourOfCash
                }
            }).width(150f).height(125f)
            ownedTeamTable.row()
        }

        teamIncomeLabel.setText(teamIncome.toString())
    }

    private fun purchaseUpgrade(itemNum : Int) {
        val upgradeName = upgradeNames[itemNum]
        try {
            upgrades.run {
                GameStateMonitor.buyUpgrade(itemNum, third[itemNum])
            }
            upgradeLabel.setText(upgradeName.substring(0, upgradeName.indexOf('\n')) + " purchased!")
            upgrades.first[itemNum]++
            timeOfPurchase = System.currentTimeMillis()
            showUpgrades()
        } catch (e : NotEnoughResourceException) {
            showErrorMessage(e.message)
        }
    }

    private fun purchaseProgrammer(itemNum: Int) {
        try {
            GameStateMonitor.hireProgrammer(programmers[itemNum])
            shop.run {
                if(programmers.size > 1) {
                    removeProgrammerFromOffer(itemNum)
                } else {
                    generateProgrammerOffer(3, 1)
                }
            }
            programmers = shop.showProgrammerOffer()
            showProgrammers()
        } catch(e : NotEnoughResourceException) {
            showErrorMessage(e.message)
        }
    }

    private fun purchaseCaffeine(itemNum: Int) {
        try {
            caffeineOffers.run {
                GameStateMonitor.buyCaffeine(first[itemNum], second[itemNum])
            }
            caffeineNames = shop.getCaffeineNamesList()
            caffeineOffers = shop.showCaffeineOffer()
            showCaffeineOffers()
        } catch (e : NotEnoughResourceException) {
            showErrorMessage(e.message)
        }
    }

    private fun startContract(difficulty: Int, itemNum: Int) {
        try {
            TaskManager.initializeContract(difficulty)
            contractTime = System.currentTimeMillis()
            contractTable.isVisible = false
            contractRunning = true
            currentContract = logoTextures[itemNum]
            currentContractTable.isVisible = true
            contractNameLabel.setText("Contract: ${contractNames[itemNum]}")
            contractDifficultyLabel.setText("Difficulty: ${difficulties[difficulty]}")
        } catch(e : NotEnoughResourceException) {
            showErrorMessage(e.message)
        }
    }

    fun updateContractCompletionLabel(progress : Float) {
        contractProgressBar.value = progress
        if(progress >= 1f) {
            TaskManager.endContract()
            contractTime = 0L
            contractProgress = 0f
            contractRunning = false
        }
    }

    fun updateContractCosts(costs: Triple<Int, Int, Int>) {
        contractCosts[0] = costs.first
        contractCosts[1] = costs.second
        contractCosts[2] = costs.third
        showContracts()
    }

    fun updateUpgradeOffer(upgrades: Triple<IntArray, IntArray, IntArray>) {
        this.upgrades = upgrades
    }

    private fun exitGame() {
        Gdx.app.exit()
    }

    companion object {
        var currentLines = 0
        var currentCash = 0
        var currentCaffeine = 0
        var errorMessage = ""
        var contractMessage = ""
        var messageTime = 0L
        var errorTime = 0L


        fun updateCurrentLineCount(count: Int) {
            currentLines = count
        }

        fun updateCurrentCashCount(count: Int) {
            currentCash = count
        }

        fun updateCurrentCaffeineCount(count: Int) {
            currentCaffeine = count
        }

        fun showErrorMessage(msg: String) {
            errorMessage = msg
            errorTime = System.currentTimeMillis()
        }

        fun showContractStatusMessage(msg: String) {
            contractMessage = msg
            messageTime = System.currentTimeMillis()
        }
    }
}
