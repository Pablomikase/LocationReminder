package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseUserLiveData: LiveData<FirebaseUser?>() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val autheStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value = firebaseAuth.currentUser
    }

    override fun onActive() {
        firebaseAuth.addAuthStateListener { autheStateListener }
    }

    override fun onInactive() {
        firebaseAuth.removeAuthStateListener { autheStateListener }
    }


}