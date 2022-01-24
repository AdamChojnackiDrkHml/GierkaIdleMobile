package Database

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import gamestate.Gamestate
import gamestate.GamestateData
import org.bson.Document
import org.bson.json.JsonObject
import java.math.BigInteger
import java.security.MessageDigest


object DatabaseUtils {
    private const val MAX_CONNECTION_TRIES = 5
    private var database_instance: MongoDatabase? = null
    private lateinit var login: String


    /**
     * @return if logging was successful
     * Checks if database instance was created and if not, tries to do so as admin
     */
    fun database_login(){
    }


    /**
     * @param login User login string
     * @param password User password string
     * @return true if login was successful, false otherwise
     * Creates instance of database with user logged
     */
    fun database_login(login: String, password: String) {

    }


    /**
     * @param login User login string
     * @param password User password string
     * @return true if user adding was successful, false otherwise
     * Adds new user to database
     */
    fun database_createUser(login: String, password: String) {

    }

    /**
     * @return JsonObject with logged user gamestate
     * Throws an error if no gamestate is found
     */
    fun database_getGamestate(): JsonObject {

        return JsonObject(Gamestate().toJson())
    }

    /**
     * @param gamestate JsonObject with logged user gamestate
     * Updates gamestate record in database for current user
     */
    fun database_updateGamestate(gamestate: JsonObject) {
    }

    /**
     * @param isUserLogging name explaining
     * @return true if connection was successful
     * Connect to database with param given user type. Then save database instance in database_instance
     */
    private fun database_connect(isUserLogging: Boolean): Boolean {
        return true
    }



    /**
     * @return if logging was successful
     * Tries to connect as user
     */
    private fun database_login_user() {
        database_init_user()
    }


    /**
     * @throws DatabaseException if database connection was unsuccessful
     * Inits database as user
     */
    private fun database_init_user() {
        database_init(true)
    }


    /**
     * @throws DatabaseException if database connection was unsuccessful
     * Inits database as admin
     */
    private fun database_init_admin() {
        database_init(false)
    }


    /**
     * @param isUser used to specify which type of connection to use
     * @throws DatabaseException if database connection was unsuccessful
     * Takes several tries to connect to database as given user type, throws exception if unsuccessful
     */
    private fun database_init(isUser: Boolean) {
        var i = 0
        while (i < MAX_CONNECTION_TRIES) {
            if (database_connect(isUser)) {
                i = MAX_CONNECTION_TRIES
            }
            i++
        }
        if (database_instance == null) {
            throw DatabaseException("Cannot connect to database")
        }
    }


    private fun database_hashString(input:String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }


    /**
     * FOR TEST PURPOSE ONLY
     */
    fun database_isConnected() : Boolean {
        return database_instance != null
    }
}