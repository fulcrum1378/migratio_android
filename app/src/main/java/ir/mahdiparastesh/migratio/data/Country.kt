package ir.mahdiparastesh.migratio.data

import java.text.Collator
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ir.mahdiparastesh.migratio.Fun.Companion.countryNames
import java.util.*
import kotlin.collections.HashMap

@Suppress("UNCHECKED_CAST")
@Entity
data class Country(
    @PrimaryKey(autoGenerate = false) var id: Long,
    @ColumnInfo(name = TAG) var tag: String,
    @ColumnInfo(name = CONTINENT) var continent: Int,
    @ColumnInfo(name = ATTRS) var attrs: HashMap<String, String>,
    @ColumnInfo(name = EXCEPT) var except: HashMap<String, String>
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        tag = parcel.readString()!!,
        continent = parcel.readInt(),
        attrs = parcel.readSerializable() as HashMap<String, String>,
        except = parcel.readSerializable() as HashMap<String, String>
    )

    override fun writeToParcel(out: Parcel?, flags: Int) {
        out?.writeLong(id)
        out?.writeString(tag)
        out?.writeInt(continent)
        out?.writeSerializable(attrs)
        out?.writeSerializable(except)
    }

    override fun describeContents() = 0

    @Suppress("unused")
    companion object {
        const val COUNTRY = "country"
        const val ID = "id"
        const val TAG = "tag"
        const val CONTINENT = "continent"
        const val ATTRS = "attrs"
        const val EXCEPT = "except"

        @JvmField
        val CREATOR = object : Parcelable.Creator<Country> {
            override fun createFromParcel(parcel: Parcel) = Country(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Country>(size)
        }

        class SortCon(val by: Int = 0) : Comparator<Country> {
            override fun compare(a: Country, b: Country) = when (by) {
                1 -> Collator.getInstance(Locale("fa")).compare(
                    countryNames()[ir.mahdiparastesh.migratio.Countries.TAGS.indexOf(a.tag)],
                    countryNames()[ir.mahdiparastesh.migratio.Countries.TAGS.indexOf(b.tag)]
                )
                else -> a.id.compareTo(b.id)
            }
        }
    }
}
