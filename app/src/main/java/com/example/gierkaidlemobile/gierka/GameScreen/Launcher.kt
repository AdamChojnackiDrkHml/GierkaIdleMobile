package GameScreen

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import concurrency.TaskManager

const val screenWidth = 1280
const val screenHeight = 720

fun main() {
    val config  = LwjglApplicationConfiguration()
    config.title = "Gierka Idle"
    config.width = screenWidth
    config.height = screenHeight
    config.resizable = false
    config.forceExit = true
    TaskManager.asyncConnectToDB()
    LwjglApplication(IdleGame(), config)
}
