package com.thakurnitin2684.screentimerank

import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_google_sign_in.*

private const val TAG = "GoogleActivity"
private const val RC_SIGN_IN = 9001

class GoogleSignInActivity : AppCompatActivity(), View.OnClickListener {
    private var mAuth: FirebaseAuth? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var myDatabase: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_sign_in)

        if (!checKPermission()) {
            val builder = AlertDialog.Builder(this)

            builder.setMessage(getString(R.string.permissionMessage))
                .setCancelable(false)
                .setPositiveButton(
                    "Give Permission"
                ) { dialog, id ->
//                    dialog.cancel()
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    Toast.makeText(
                        applicationContext, "OPEN THE APP AGAIN",
                        Toast.LENGTH_SHORT
                    ).show()
                    this.finishAffinity()

                    Toast.makeText(
                        applicationContext, "you choose yes action for permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton(
                    "Deny"
                ) { dialog, id ->
                    this.finishAffinity()
                    Toast.makeText(
                        applicationContext, "you choose no action for permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            val alert = builder.create()

            alert.setTitle("App Permissions")
            alert.show()
        }
        sign_in_button.setOnClickListener(this)
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        mAuth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
  val currentUser = mAuth!!.currentUser

        if (currentUser != null && checKPermission()) {
            Log.d(TAG, "Currently Signed in: " + currentUser.email!!)

            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.sign_in_button) {
            signInToGoogle()
        }
    }

    private fun signInToGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                 val account = task.getResult(ApiException::class.java)

                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
           }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth!!.currentUser
                    myDatabase = Database()
                    myDatabase!!.open()
                    if (acct != null && user != null) {
                        myDatabase.addDbForFirstTime(
                            user.uid,
                            acct.displayName,
                            acct.email,
                            acct.photoUrl.toString()
                        )
                        var intent = Intent(this@GoogleSignInActivity, MainActivity::class.java)
                        intent.putExtra("newBool", "Present")
                        startActivity(intent)

                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun checKPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun onBackPressed() {

    }
}
