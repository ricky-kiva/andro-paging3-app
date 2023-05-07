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
        val page = when (loadType) {
            // this to get current RemoteKeys of the page
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                // return next key minus 1 (current) if not null. If null return 1
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            // this when user scrolls up & user need to load data to the beginning of list
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                // get remoteKeys if not null
                val prevKey = remoteKeys?.prevKey ?: return MediatorResult.Success(endOfPaginationReached = (remoteKeys != null))
                prevKey
            }
            // this when user scrolls down & user need to load data to the end of list
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                // get remoteKeys if not null
                val nextKey = remoteKeys?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = (remoteKeys != null))
                nextKey
            }
        }

        return try {
            // call apiService & holds returned data
            val responseData = apiService.getQuote(page, state.config.pageSize)
            val endOfPaginationReached = responseData.isEmpty()

            database.withTransaction {
                // if LoadType is REFRESH, local database will delete all data
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.quoteDao().deleteAll()
                }

                // set remoteKey according to key
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = responseData.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                // add data to remoteKeysDao
                database.remoteKeysDao().insertAll(keys)
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

    // retrieve `RemoteKeys` from `last page` in the `list of pages`
    // return `last page RemoteKeys` or null
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, QuoteResponseItem>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    // retrieve `RemoteKeys` from `first page` in the `list of pages`
    // return `first page RemoteKeys` or null
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, QuoteResponseItem>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    // retrieve `RemoteKeys` from `closest page` from the current scroll position
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, QuoteResponseItem>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

}