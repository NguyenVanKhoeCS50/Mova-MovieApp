package com.prmto.mova_movieapp.core.util.handlePagingLoadState

import com.prmto.mova_movieapp.core.presentation.util.UiText
import com.prmto.mova_movieapp.feature_upcoming.presentation.adapter.UpComingMovieAdapter

class HandlePagingStateUpComingPagingAdapter(
    upComingPagingAdapter: UpComingMovieAdapter,
    onLoading: () -> Unit,
    onNotLoading: () -> Unit,
    onError: (UiText) -> Unit
) : BasePagingLoadState() {
    init {
        upComingPagingAdapter.addLoadStateListener { loadState ->
            handlePagingLoadState(
                loadStates = loadState,
                onLoading = onLoading,
                onNotLoading = onNotLoading,
                onError = onError
            )
        }
    }
}