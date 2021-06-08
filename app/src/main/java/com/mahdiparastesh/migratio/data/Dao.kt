package com.mahdiparastesh.migratio.data

import androidx.room.*
import androidx.room.Dao
import com.mahdiparastesh.migratio.data.Country.Companion.COUNTRY
import com.mahdiparastesh.migratio.data.Criterion.Companion.CRITERION
import com.mahdiparastesh.migratio.data.MyCriterion.Companion.MYCRITERION

@Dao
interface Dao {
    @Query("SELECT * FROM $COUNTRY")
    fun getCountries(): List<Country>

    @Query("SELECT * FROM $CRITERION")
    fun getCriteria(): List<Criterion>

    @Query("SELECT * FROM $MYCRITERION")
    fun getMyCriteria(): List<MyCriterion>

    @Query("SELECT * FROM $COUNTRY WHERE ${Country.ID} LIKE :id LIMIT 1")
    fun getCountry(id: Long): Country

    @Query("SELECT * FROM $CRITERION WHERE ${Criterion.ID} LIKE :id LIMIT 1")
    fun getCriterion(id: Long): Criterion

    @Query("SELECT * FROM $MYCRITERION WHERE ${MyCriterion.ID} LIKE :id LIMIT 1")
    fun getMyCriterion(id: Long): MyCriterion

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCountries(item: List<Country>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCriteria(item: List<Criterion>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMyCriteria(item: List<MyCriterion>)

    @Delete
    fun deleteCountries(item: List<Country>)

    @Delete
    fun deleteCriteria(item: List<Criterion>)

    @Delete
    fun deleteMyCriteria(item: List<MyCriterion>)
}
