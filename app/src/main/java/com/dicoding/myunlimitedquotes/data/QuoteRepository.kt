package com.dicoding.myunlimitedquotes.data

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.dicoding.myunlimitedquotes.database.QuoteDatabase
import com.dicoding.myunlimitedquotes.database.QuoteRemoteMediator
import com.dicoding.myunlimitedquotes.network.ApiService
import com.dicoding.myunlimitedquotes.network.QuoteResponseItem

class QuoteRepository(private val quoteDatabase: QuoteDatabase, private val apiService: ApiService) {

    // return Paging 3 with desired config
    fun getQuote(): LiveData<PagingData<QuoteResponseItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = QuoteRemoteMediator(quoteDatabase, apiService),
            pagingSourceFactory = {
                quoteDatabase.quoteDao().getAllQuote()
                /* Not used because we are using RemoteMediator
                QuotePagingSource(apiService)*/
            }
        ).liveData
    }

    /* Not used because we are using Paging 3
    suspend fun getQuote(): List<QuoteResponseItem> {
        return apiService.getQuote(1, 5)
    }*/
}