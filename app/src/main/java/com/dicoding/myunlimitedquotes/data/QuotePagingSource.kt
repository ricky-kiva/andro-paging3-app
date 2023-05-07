package com.dicoding.myunlimitedquotes.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dicoding.myunlimitedquotes.network.ApiService
import com.dicoding.myunlimitedquotes.network.QuoteResponseItem

class QuotePagingSource(private val apiService: ApiService): PagingSource<Int, QuoteResponseItem>() {

    // this to determine key to use when refreshing data source
    override fun getRefreshKey(state: PagingState<Int, QuoteResponseItem>): Int? {
        // get current item anchored on screen (will continue if not null)
        return state.anchorPosition?.let { anchorPosition ->
            // get page that is closest to anchorPosition
            // the pref/nextKey being setup on load(). This function determine the behavior of those value
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    // this to load page data from data source
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, QuoteResponseItem> {
        return try {
            // get current position of page
            val position = params.key ?: INITIAL_PAGE_INDEX
            // call apiService & holds returned data
            val responseData = apiService.getQuote(position, params.loadSize)

            // set LoadResult based on position & responseData condition
            LoadResult.Page(
                data = responseData,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else (position - 1),
                nextKey = if (responseData.isNullOrEmpty()) null else (position + 1)
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

}