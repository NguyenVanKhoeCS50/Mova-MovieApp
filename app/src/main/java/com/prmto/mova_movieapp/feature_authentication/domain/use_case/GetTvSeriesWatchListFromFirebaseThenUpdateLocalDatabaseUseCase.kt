package com.prmto.mova_movieapp.feature_authentication.domain.use_case

import com.prmto.mova_movieapp.R
import com.prmto.mova_movieapp.core.domain.repository.firebase.FirebaseCoreRepository
import com.prmto.mova_movieapp.core.domain.repository.local.LocalDatabaseRepository
import com.prmto.mova_movieapp.core.presentation.util.UiText
import com.prmto.mova_movieapp.feature_authentication.domain.repository.FirebaseTvSeriesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class GetTvSeriesWatchListFromFirebaseThenUpdateLocalDatabaseUseCase @Inject constructor(
    private val firebaseCoreRepository: FirebaseCoreRepository,
    private val firebaseTvSeriesRepository: FirebaseTvSeriesRepository,
    private val localDatabaseRepository: LocalDatabaseRepository,
) {

    operator fun invoke(
        onFailure: (uiText: UiText) -> Unit,
        coroutineScope: CoroutineScope,
    ) {
        val currentUser = firebaseCoreRepository.getCurrentUser()
        val userUid = currentUser?.uid
            ?: return onFailure(UiText.StringResource(R.string.error_user))

        firebaseTvSeriesRepository.getTvSeriesInWatchList(
            userUid = userUid,
            onSuccess = { tvSeries ->
                tvSeries.forEach { tvSeriesItem ->
                    coroutineScope.launch {
                        localDatabaseRepository.tvSeriesLocalRepository.insertTvSeriesToWatchListItem(
                            tvSeries = tvSeriesItem
                        )
                    }
                }
            },
            onFailure = onFailure
        )
    }
}