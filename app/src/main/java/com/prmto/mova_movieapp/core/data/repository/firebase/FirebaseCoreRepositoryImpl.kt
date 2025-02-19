package com.prmto.mova_movieapp.core.data.repository.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.prmto.mova_movieapp.core.domain.repository.firebase.FirebaseCoreRepository
import javax.inject.Inject

class FirebaseCoreRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : FirebaseCoreRepository {

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override fun signOut() {
        auth.signOut()
    }
}