package com.prmto.mova_movieapp.core.domain.use_case

import com.prmto.mova_movieapp.core.domain.repository.LocalDatabaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteMovieIdsUseCase @Inject constructor(
    private val repository: LocalDatabaseRepository
) {
    operator fun invoke(): Flow<List<Int>> {
        return repository.getFavoriteMovieIds()
    }
}