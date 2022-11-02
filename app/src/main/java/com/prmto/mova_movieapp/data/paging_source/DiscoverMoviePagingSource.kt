package com.prmto.mova_movieapp.data.paging_source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.prmto.mova_movieapp.data.models.toMovieList
import com.prmto.mova_movieapp.data.remote.TMDBApi
import com.prmto.mova_movieapp.domain.models.Movie
import com.prmto.mova_movieapp.presentation.filter_bottom_sheet.state.FilterBottomState
import com.prmto.mova_movieapp.presentation.util.toDiscoveryQueryString
import com.prmto.mova_movieapp.presentation.util.toSeparateWithComma
import com.prmto.mova_movieapp.util.Constants
import javax.inject.Inject


class DiscoverMoviePagingSource @Inject constructor(
    private val tmdbApi: TMDBApi,
    private val filterBottomState: FilterBottomState,
    private val language: String
) : PagingSource<Int, Movie>() {
    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {

        val nextPage = params.key ?: Constants.STARTING_PAGE

        return try {
            val apiResponse = tmdbApi.discoverMovie(
                page = nextPage,
                language = language,
                genres = filterBottomState.checkedGenreIdsState.toSeparateWithComma(),
                releaseYear = filterBottomState.checkedPeriodId,
                sort = filterBottomState.checkedSortState.toDiscoveryQueryString(filterBottomState.categoryState),
            )

            LoadResult.Page(
                data = apiResponse.results.toMovieList(),
                prevKey = if (nextPage == 1) null else nextPage.minus(1),
                nextKey = if (nextPage < apiResponse.totalPages) nextPage.plus(1) else null
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    }
}