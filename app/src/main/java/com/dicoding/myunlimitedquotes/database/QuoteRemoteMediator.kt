package com.dicoding.myunlimitedquotes.database

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dicoding.myunlimitedquotes.network.ApiService
import com.dicoding.myunlimitedquotes.network.QuoteResponseItem

@OptIn(ExperimentalPagingApi::class)
class QuoteRemoteMediator (
    private val database: QuoteDatabase,
    private val apiService: ApiService
): RemoteMediator<Int, QuoteResponseItem>() {

    override suspend fun initialize(): InitializeAction {
        // do initial refresh to load first set of data
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    // this loads data from network or local database & store the data in the database
    // LoadType indicates whether it's a NEW LOAD or a REFRESH
    // state provides current state of PagingData stream
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, QuoteResponseItem>
    ): MediatorResult {
        val page = INITIAL_PAGE_INDEX

        return try {
            // call apiService & holds returned data
            val responseData = apiService.getQuote(page, state.config.pageSize)
            val endOfPaginationReached = responseData.isEmpty()

            database.withTransaction {
                // if LoadType is REFRESH, local database will delete all data
                if (loadType == LoadType.REFRESH) {
                    database.quoteDao().deleteAll()
                }
                // insert new data to database
                database.quoteDao().insertQuote(responseData)
            }
            // return success/fail in MediatorResult process
            // if responseData is empty, endOnPaginationReached set to true
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

}