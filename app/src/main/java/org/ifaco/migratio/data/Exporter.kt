package org.ifaco.migratio.data

import android.net.Uri
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import org.ifaco.migratio.Fun.Companion.c
import org.ifaco.migratio.Fun.Companion.z
import org.ifaco.migratio.R
import org.ifaco.migratio.Select
import org.ifaco.migratio.more.FileUtils
import java.io.*
import java.lang.Exception
import java.util.*

class Exporter {
    companion object {
        fun export(path: String): Boolean {
            if (Select.myCountries == null || Select.myCriteria == null) return false
            val d = Calendar.getInstance()
            val date =
                "${d[Calendar.YEAR]}-${z(d[Calendar.MONTH] + 1)}-${z(d[Calendar.DAY_OF_MONTH])}"
            val time =
                "${z(d[Calendar.HOUR_OF_DAY])}-${z(d[Calendar.MINUTE])}-${z(d[Calendar.SECOND])}"
            val file = File(path, "Migratio Settings $date - $time.json")
            if (file.exists()) file.delete()
            val pack = Exported(Select.myCountries!!, Select.myCriteria!!)
            try {
                FileOutputStream(file, false).apply {
                    write(Gson().toJson(pack).toByteArray())
                    close()
                }
            } catch (ignored: IOException) {
            }
            return file.exists()
        }

        fun import(uri: Uri): Exported? {
            var r: JsonReader? = null
            try {
                r = JsonReader(InputStreamReader(FileInputStream(File(FileUtils.getPath(c, uri)))))
            } catch (e: Exception) {
                Toast.makeText(
                    c, c.resources.getString(R.string.importOpenError), Toast.LENGTH_LONG
                ).show()
            }
            return try {
                Gson().fromJson<Exported>(r, Exported::class.java)
            } catch (e: Exception) {
                Toast.makeText(
                    c, c.resources.getString(R.string.importReadError), Toast.LENGTH_LONG
                ).show()
                null
            }
        }
    }

    data class Exported(var MYCON: MutableSet<String>, var MYCRI: ArrayList<MyCriterion>)
}