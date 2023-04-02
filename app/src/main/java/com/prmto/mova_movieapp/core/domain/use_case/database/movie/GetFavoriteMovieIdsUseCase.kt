package com.prmto.mova_movieapp.core.domain.use_case.database.movie

import com.prmto.mova_movieapp.core.domain.repository.local.LocalDatabaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteMovieIdsUseCase @Inject constructor(
    private val repository: LocalDatabaseRepository
) {
    operator fun invoke(): Flow<List<Int>> {
        return repository.movieLocalRepository.getFavoriteMovieIds()
    }
}