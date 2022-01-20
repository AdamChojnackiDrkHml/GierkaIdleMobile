package GameScreen

import GameScreen.gamescreen.*
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.scene2d.Scene2DSkin

/**
 * Main game class
 */
class IdleGame: Game() {
    private lateinit var initialScreen : InitialScreen
    private lateinit var loginScreen : LoginScreen
    private lateinit var signinScreen : SigninScreen
    private lateinit var gameScreen : GameScreen
    lateinit var batch : SpriteBatch
    lateinit var font : BitmapFont
    lateinit var camera : OrthographicCamera
    lateinit var skinFile : FileHandle
    lateinit var screenBackgroundTexture : Texture

    override fun create() {
        skinFile = Gdx.files.internal("assets/uiskin.json")
        screenBackgroundTexture = Texture(Gdx.files.internal("assets/B_Office.png"))

        batch = SpriteBatch()
        font = BitmapFont()
        camera = OrthographicCamera()
        camera.setToOrtho(false, screenWidth.toFloat(), screenHeight.toFloat())

        Scene2DSkin.defaultSkin = Skin(skinFile)

        initialScreen = InitialScreen(this)
        loginScreen = LoginScreen(this)
        signinScreen = SigninScreen(this)
        gameScreen = GameScreen(this)

        setScreen(initialScreen)
    }

    override fun dispose() {
        gameScreen.dispose()
        batch.dispose()
        font.dispose()
        screenBackgroundTexture.dispose()
    }

    /**
     * Function used by various in-game screens to change between them
     * @param name Screen name string
     */
    fun setGameScreen(name: String) {
        when(name) {
            "initial" -> setScreen(initialScreen)
            "login" -> setScreen(loginScreen)
            "signin" -> setScreen(signinScreen)
            "game" -> {
                setScreen(gameScreen)
                gameScreen.enter()
                initialScreen.dispose()
                loginScreen.dispose()
                signinScreen.dispose()
            }
        }
    }
}