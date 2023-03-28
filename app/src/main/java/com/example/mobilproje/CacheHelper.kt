import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson

class CacheHelper {

    companion object {

        private const val CACHE_NAME = "app_cache"
        private const val KEY_GRADUAT_PERSON = "graduat_person"

        fun cacheGraduatPerson(context: Context, graduatPerson: GraduatPerson) {
            val gson = Gson()
            val json = gson.toJson(graduatPerson)
            val prefs = context.getSharedPreferences(CACHE_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                putString(KEY_GRADUAT_PERSON, json)
                apply()
            }
        }

        fun getCachedGraduatPerson(context: Context): GraduatPerson? {
            val gson = Gson()
            val prefs = context.getSharedPreferences(CACHE_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_GRADUAT_PERSON, null)
            return if (json != null) {
                gson.fromJson(json, GraduatPerson::class.java)
            } else {
                null
            }
        }

    }

}
