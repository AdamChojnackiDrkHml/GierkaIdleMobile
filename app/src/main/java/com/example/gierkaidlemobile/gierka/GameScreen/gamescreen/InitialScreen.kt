package GameScreen.gamescreen

import GameScreen.IdleGame
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.actors.centerPosition
import ktx.actors.onClick
import ktx.app.clearScreen
import ktx.scene2d.*

/**
 * Initial screen from which user is allowed to log in or create an account
 * @param game Instance of IdleGame class
 */
class InitialScreen(private val game: IdleGame) : Screen {
    private var stage = Stage()
    private var initialWindow : KWindow
    private var loginButton = scene2d.textButton(text = "Log in")
    private var signinButton = scene2d.textButton(text = "Sign in")
    private var backgroundTable = scene2d.table {
        background(TextureRegionDrawable(TextureRegion(game.screenBackgroundTexture)))
        setFillParent(true)
    }

    init {
        loginButton.onClick { game.setGameScreen("login") }
        signinButton.onClick { game.setGameScreen("signin") }

        initialWindow = scene2d.window(title = "Welcome!") {
            width = 400f
            height = 300f
            isMovable = false
            add(loginButton).width(200f).height(50f)
            row()
            add(signinButton).width(200f).height(50f).pad(10f, 0f, 10f, 0f)
        }

        stage.addActor(backgroundTable)
        stage.addActor(initialWindow)
        initialWindow.centerPosition()
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
