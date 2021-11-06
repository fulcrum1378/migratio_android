package ir.mahdiparastesh.migratio.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Country::class, Criterion::class, MyCriterion::class],
    version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun dao(): Dao
}
