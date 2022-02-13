package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private lateinit var binding: ActivityAuthenticationBinding
    private val viewModel by viewModels<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//         : Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          : If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        //BInding
        //binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_authentication, null, false)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeAuthenticationState()

        binding.loginButton.setOnClickListener {
            launchSignInFlow()
        }


    }

    private fun launchSignInFlow() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            AuthenticationActivity.SIGN_IN_RESULT_CODE
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if(resultCode == Activity.RESULT_OK){
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}")

                //Navigation to the dashboard
                val myIntent = Intent(this, RemindersActivity::class.java)
                startActivity(myIntent)

            } else{

                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
                Toast.makeText(this, getString(R.string.error_in_login) + "${response?.error?.errorCode}", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun observeAuthenticationState() {
        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            when(authenticationState){

                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    binding.loginButton.text = getString(R.string.loadingButtonText)
                }
                else -> {
                    binding.loginButton.text = getString(R.string.login)
                }

            }
        })
    }

}
