package org.ifaco.migratio.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Comparator

@Entity
data class MyCriterion(
    @PrimaryKey(autoGenerate = true) var id: Long,
    @ColumnInfo(name = TAG) var tag: String,
    @ColumnInfo(name = IS_ON) var isOn: Boolean,
    @ColumnInfo(name = GOOD) var good: String,
    @ColumnInfo(name = IMPORTANCE) var importance: Int
) : Parcelable {
    private constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        tag = parcel.readString()!!,
        isOn = parcel.readByte() == (1).toByte(),
        good = parcel.readString()!!,
        importance = parcel.readInt()
    )

    override fun writeToParcel(out: Parcel?, flags: Int) {
        out?.writeLong(id)
        out?.writeString(tag)
        out?.writeByte(if (isOn) 1 else 2)
        out?.writeString(good)
        out?.writeInt(importance)
    }

    override fun describeContents() = 0

    @Suppress("unused", "SpellCheckingInspection")
    companion object {
        const val MYCRITERION = "mycriterion"
        const val ID = "id"
        const val TAG = "tag"
        const val IS_ON = "isOn"
        const val GOOD = "good"
        const val IMPORTANCE = "importance"

        @JvmField
        val CREATOR = object : Parcelable.Creator<MyCriterion> {
            override fun createFromParcel(parcel: Parcel) = MyCriterion(parcel)
            override fun newArray(size: Int) = arrayOfNulls<MyCriterion>(size)
        }

        class SortMyCri : Comparator<MyCriterion> {
            override fun compare(a: MyCriterion, b: MyCriterion) = a.tag.compareTo(b.tag)
        }
    }
}