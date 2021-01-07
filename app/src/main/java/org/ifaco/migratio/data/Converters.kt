package org.ifaco.migratio.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun stringToHashMap(value: String): HashMap<String, String> =
        Gson().fromJson(value, object : TypeToken<HashMap<String, String>>() {}.type)

    @TypeConverter
    fun hashMapToString(map: HashMap<String, String>): String = Gson().toJson(map)

    /*@TypeConverter
    fun stringToList(value: String): List<String> =
        Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun listToString(list: List<String>): String = Gson().toJson(list)*/
}