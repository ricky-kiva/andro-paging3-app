package com.dicoding.myunlimitedquotes.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.myunlimitedquotes.network.QuoteResponseItem

@Dao
interface QuoteDao {
    // if there is conflict when inserting, existing row will be replaced with new data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: List<QuoteResponseItem>)

    @Query("Select * FROM quote")
    fun getAllQuote(): PagingSource<Int, QuoteResponseItem>

    @Query("DELETE FROM quote")
    suspend fun deleteAll()
}