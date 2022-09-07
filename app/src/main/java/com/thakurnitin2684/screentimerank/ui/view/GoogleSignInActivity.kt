package com.thakurnitin2684.screentimerank.ui.view

import android.app.Activity
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.thakurnitin2684.screentimerank.R
import com.thakurnitin2684.screentimerank.data.User
import com.thakurnitin2684.screentimerank.databinding.ActivityGoogleSignInBinding
import com.thakurnitin2684.screentimerank.ui.AuthListener
import com.thakurnitin2684.screentimerank.ui.viewmodel.AuthViewModel
import com.thakurnitin2684.screentimerank.ui.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "GoogleActivity"

@AndroidEntryPoint
class GoogleSignInActivity : AppCompatActivity(), AuthListener {

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val authViewModel: AuthViewModel by viewModels()
    private val userProfileViewModel: UserProfileViewModel by viewModels()


    private lateinit var activityGoogleSignInBinding: ActivityGoogleSignInBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityGoogleSignInBinding = ActivityGoogleSignInBinding.inflate(layoutInflater)
        setContentView(activityGoogleSignInBinding.root)


        authViewModel.authListener = this

        if (!checkPermission()) {
            val builder = AlertDialog.Builder(this)

            builder.setMessage(getString(R.string.permissionMessage))
                .setCancelable(false)
                .setPositiveButton(
                    "Give Permission"
                ) { _, _ ->
//                    dialog.cancel()
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    Toast.makeText(
                        applicationContext, "OPEN THE APP AGAIN",
                        Toast.LENGTH_SHORT
                    ).show()
                    this.finishAffinity()

                    Toast.makeText(
                        applicationContext, "you chose yes action for permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton(
                    "Deny"
                ) { _, _ ->
                    this.finishAffinity()
                    Toast.makeText(
                        applicationContext, "you chose no action for permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            val alert = builder.create()

            alert.setTitle("App Permissions")
            alert.show()
        }
        activityGoogleSignInBinding.signInButton.setOnClickListener {
            signInToGoogle()
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    public override fun onStart() {
        super.onStart()
        val currentUser = authViewModel.getCurrentUser()
        if (currentUser != null && checkPermission()) {
            Log.d(TAG, "Currently Signed in: " + currentUser.email!!)
            startActivity(Intent(this, MainActivity::class.java))
        }
    }


    private fun signInToGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)

    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {

                    val account = task.getResult(ApiException::class.java)

                    firebaseAuthWithGoogle(account!!)
                } catch (e: ApiException) {
                    Log.d(TAG, "failed here")
                    Log.w(TAG, "Google sign in failed", e)
                }

            }
        }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        authViewModel.signIn(credential, acct)
    }

    private fun checkPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun onBackPressed() {

    }

    override fun onStarted() {

    }

    override fun onSuccess(acct: GoogleSignInAccount) {
        Log.d(TAG, "On Success")

        val user = authViewModel.getCurrentUser()
        if (user != null) {
            userProfileViewModel.addDbforFirstTime(
                user.uid, User(
                    acct.displayName,
                    acct.email,
                    acct.photoUrl.toString(),
                    0,
                    arrayListOf()
                )
            )
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("newBool", "Present")
            startActivity(intent)
        }
    }

    override fun onFailure() {
        Log.d(TAG, "On Fail")

        Log.w(TAG, "signInWithCredential:failure")

    }
}
