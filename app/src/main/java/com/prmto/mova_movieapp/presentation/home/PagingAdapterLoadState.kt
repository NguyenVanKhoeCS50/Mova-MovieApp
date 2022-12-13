package com.prmto.mova_movieapp.presentation.home

import com.prmto.mova_movieapp.presentation.util.UiText

data class PagingAdapterLoadState(
    val nowPlayingState: PagingAdapterLoadStateItem = PagingAdapterLoadStateItem(),
    val popularMoviesState: PagingAdapterLoadStateItem = PagingAdapterLoadStateItem(),
    val popularTvSeriesState: PagingAdapterLoadStateItem = PagingAdapterLoadStateItem(),
    val topRatedMoviesState: PagingAdapterLoadStateItem = PagingAdapterLoadStateItem(),
    val topRatedTvSeriesState: PagingAdapterLoadStateItem = PagingAdapterLoadStateItem(),
)

data class PagingAdapterLoadStateItem(
    val isLoading: Boolean = false,
    val error: UiText? = null
)