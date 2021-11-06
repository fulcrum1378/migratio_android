package ir.mahdiparastesh.migratio.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun stringToHashMap(value: String): HashMap<String, String> =
        Gson().fromJson(value, object : TypeToken<HashMap<String, String>>() {}.type)

    @TypeConverter
    fun hashMapToString(map: HashMap<String, String>): String = Gson().toJson(map)
}