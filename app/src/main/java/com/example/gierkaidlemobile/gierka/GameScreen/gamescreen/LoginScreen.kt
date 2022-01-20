package GameScreen.gamescreen

import Database.DatabaseUtils
import GameScreen.IdleGame
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import concurrency.TaskManager
import ktx.actors.centerPosition
import ktx.actors.onClick
import ktx.app.clearScreen
import ktx.scene2d.*

/**
 * Log in screen which takes user to actual game screen after successful login
 * @param game Instance of IdleGame class
 */
class LoginScreen(private val game: IdleGame) : Screen {
    private var stage = Stage()
    private var loginWindow : KWindow
    private var loginLabel = scene2d.label(text = "Login")
    private var passwordLabel = scene2d.label(text = "Password")
    private var loginTextField = scene2d.textField()
    private var passwordTextField = scene2d.textField {
        isPasswordMode = true
        setPasswordCharacter('*')
    }
    private var confirmButton = scene2d.textButton(text = "Continue")
    private var backButton = scene2d.textButton(text = "Back")
    private var infoLabel = scene2d.label(text = "")
    private var backgroundTable = scene2d.table {
        background(TextureRegionDrawable(TextureRegion(game.screenBackgroundTexture)))
        setFillParent(true)
    }

    init {
        confirmButton.onClick { logIn() }
        backButton.onClick {
            clearTextFields()
            infoLabel.setText("")
            game.setGameScreen("initial")
        }

        loginWindow = scene2d.window(title = "Log in") {
            width = 400f
            height = 300f
            isMovable = false

            table {
                defaults().pad(5f)
                add(loginLabel)
                add(loginTextField)
                row()
                add(passwordLabel)
                add(passwordTextField)
                it.spaceBottom(10f).row()
            }

            table {
                defaults().pad(5f)
                add(backButton).width(125f).height(30f)
                add(confirmButton).width(125f).height(30f)
                it.spaceBottom(10f).row()
            }

            add(infoLabel)
        }

        stage.addActor(backgroundTable)
        stage.addActor(loginWindow)
        loginWindow.centerPosition()
    }

    /**
     * Called on pressing confirm button in login window, performs login operation
     * using DatabaseUtils
     */
    private fun logIn() {
        val login = loginTextField.text
        val password = passwordTextField.text

        clearTextFields()

        try {
            DatabaseUtils.database_login(login, password)
            game.setGameScreen("game")
            TaskManager.startTasks()
        } catch(e: Exception) {
            infoLabel.color = Color.FIREBRICK
            infoLabel.setText(e.message)
        }
    }

    /**
     * Removes any text from login and password text fields
     */
    private fun clearTextFields() {
        loginTextField.text = ""
        passwordTextField.text = ""
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 0f)
        stage.act()
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {}

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    override fun dispose() {
        stage.dispose()
    }
}
