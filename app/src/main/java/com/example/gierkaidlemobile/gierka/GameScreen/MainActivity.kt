package GameScreen

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import concurrency.GameStateMonitor
import concurrency.TaskManager

class MainActivity : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        TaskManager.asyncConnectToDB()
        val game = IdleGame()
        GameStateMonitor.init(game)
        initialize(game, config)
    }
}