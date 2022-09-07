package com.thakurnitin2684.screentimerank.ui.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.messaging.FirebaseMessaging
import com.thakurnitin2684.screentimerank.BuildConfig
import com.thakurnitin2684.screentimerank.R
import com.thakurnitin2684.screentimerank.data.RoomData
import com.thakurnitin2684.screentimerank.data.User
import com.thakurnitin2684.screentimerank.databinding.AboutBinding
import com.thakurnitin2684.screentimerank.databinding.ActivityMainBinding
import com.thakurnitin2684.screentimerank.ui.adapter.UserDataAdapter
import com.thakurnitin2684.screentimerank.ui.viewmodel.AuthViewModel
import com.thakurnitin2684.screentimerank.ui.viewmodel.ScreenTimeViewModel
import com.thakurnitin2684.screentimerank.ui.viewmodel.UserProfileViewModel
import com.thakurnitin2684.screentimerank.utils.AlarmDatabase
import com.thakurnitin2684.screentimerank.utils.PrefRepository


private const val TAG = "MainActivity"



class AppUsageInfo internal constructor(var packageName: String) {
    var timeInForeground: Long = 0
    var launchCount = 0
}

class MainActivity : AppCompatActivity(), UserDataAdapter.OnTaskClickListener {


    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var alarmHandler = AlarmDatabase(this)
    private  var interAd: InterstitialAd? =null
    private var aboutDialog: AlertDialog? = null


    private val authViewModel: AuthViewModel by viewModels()
    private val userProfileViewModel: UserProfileViewModel by viewModels()
    private val screenTimeViewModel: ScreenTimeViewModel by viewModels()

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var aboutBinding: AboutBinding


    lateinit var userDataAdapter: UserDataAdapter





    private var userId: String? = null
    private val roomData = mutableListOf<RoomData>()
    private var roomIds = ArrayList<String>()


    private val   prefRepository by lazy { PrefRepository(applicationContext) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(activityMainBinding.root)



        userId = authViewModel.getCurrentUser()?.uid


//        //For alarm manager
        alarmHandler.cancelAlarmManager()
        alarmHandler.setAlarmManager()


        //Ads
        MobileAds.initialize(
            this
        ) {

        }



        //Cloud Messaging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = "Topic"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        FirebaseMessaging.getInstance().subscribeToTopic("AllTopic")
            .addOnCompleteListener {
            }


        //Adapter Inflation
        activityMainBinding.mainListView.layoutManager = LinearLayoutManager(this)
        userDataAdapter = UserDataAdapter(this, mutableListOf(), null, 0,null)
        activityMainBinding.mainListView.addItemDecoration(
            DividerItemDecoration(
                activityMainBinding.mainListView.context,
                LinearLayout.SHOW_DIVIDER_NONE
            )
        )
        activityMainBinding.mainListView.adapter = userDataAdapter



        userProfileViewModel.getUserDetails(userId!!)

        val inAnimation = AlphaAnimation(0f, 1f)
        inAnimation.duration = 200
        activityMainBinding.progressBarHolder.animation = inAnimation
        activityMainBinding.progressBarHolder.visibility = View.VISIBLE


        var thisUser = User("name", "email", "url", 0, arrayListOf())


        userProfileViewModel.userProfile.observe(this) { user ->
            if (user != null) {
                thisUser = user

                userDataAdapter.addHeaderData(user)
                userDataAdapter.notifyItemChanged(0)





                if (user.rooms.size == 0) {
                    val outAnimation = AlphaAnimation(1f, 0f)
                    outAnimation.duration = 200
                    activityMainBinding.progressBarHolder.animation = outAnimation
                    activityMainBinding.progressBarHolder.visibility = View.GONE
                }



                roomIds = user.rooms
                userProfileViewModel.getRoomDetails(user.rooms)


            }
        }



        userProfileViewModel.roomDetails.observe(this) { rooms ->


            if (rooms != null) {

                for (position in 0 until rooms.size) {

                    roomData.add(RoomData())

                    val currentRoom = rooms[position]
                    roomData[position].roomId = roomIds[position]


                    if (currentRoom != null) {
                        roomData[position].roomName = currentRoom.name
                    }
                    if (currentRoom != null) {
                        roomData[position].members = currentRoom.members
                    }
                }

                userProfileViewModel.getAllUserDetails(roomData)


                userDataAdapter.addData(roomData)
                userDataAdapter.notifyDataSetChanged()

                val outAnimation = AlphaAnimation(1f, 0f)
                outAnimation.duration = 200
                activityMainBinding.progressBarHolder.animation = outAnimation
                activityMainBinding.progressBarHolder.visibility = View.GONE
            }
        }





        userProfileViewModel.allUsers.observe(this) { allRoomUsers ->
            if (allRoomUsers != null) {

                for (room in 0 until allRoomUsers.size) {

                    val screenTImeList = ArrayList<Long>()


                    for (user in 0 until allRoomUsers[room].size) {


                        allRoomUsers[room][user]?.screenTime?.let { screenTImeList.add(it) }
                        allRoomUsers[room][user]?.let {
                            it.name?.let { it1 ->
                                roomData[room].membersName.add(
                                    it1
                                )
                            }
                        }


                        allRoomUsers[room][user]?.let { roomData[room].membersUrl.add(it.url) }

                    }

                    screenTImeList.sort()
                    val cRank =
                        (screenTImeList.binarySearch(thisUser.screenTime) + 1)
                    roomData[room].rank =
                        (roomData[room].members.size - cRank + 1).toString()



                    if (room == allRoomUsers.size-1) {



                        var adLoader: AdLoader?
                        val builder = AdLoader.Builder(
                            this,
                            getString(R.string.nativeAdId)
                        )
                        adLoader =
                            builder.forNativeAd { nativeAd ->
                                // A native ad loaded successfully, check if the ad loader has finished loading
                                // and if so, insert the ads into the list.

                                if (thisUser.rooms.size > 2) {

                                    userDataAdapter.addAdData(nativeAd)
                                    userDataAdapter.notifyDataSetChanged()
                                }
//


                            }.build()
                        adLoader.loadAd(AdRequest.Builder().build())
                        /* ********************************************** */
                    }


                }
                userDataAdapter.addData(roomData)
                userDataAdapter.notifyDataSetChanged()
            }

        }



        screenTimeViewModel.screenTime()

        userDataAdapter.addHeaderTime(screenTimeViewModel.totalTime)
        userDataAdapter.notifyItemChanged(0)




        screenTimeViewModel.screenTimeDatabase()

        if (prefRepository.getIsFirstTime()) {
            userProfileViewModel.updateTime(userId,screenTimeViewModel.totalScreenTime)
        }

        val bl =  prefRepository.getUnivInt()

        if (screenTimeViewModel.hourInt != bl) {
            userProfileViewModel.updateTime(userId,screenTimeViewModel.totalScreenTime)
            prefRepository.setUnivInt(screenTimeViewModel.hourInt)


        }


        // SetOnRefreshListener on SwipeRefreshLayout
        activityMainBinding.swipeRefreshLayout.setOnRefreshListener(OnRefreshListener {
            activityMainBinding.swipeRefreshLayout.isRefreshing = false
            finish()
            startActivity(intent)

        })

        requestInterAd()


        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                var deeplink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deeplink = pendingDynamicLinkData.link
                }
                if (deeplink != null) {
                    val id = deeplink.getQueryParameter("data")
                    Toast.makeText(applicationContext, "New Room Added", Toast.LENGTH_SHORT)
                        .show()
                    if (id != null) {
                        userProfileViewModel.addForRoom(userId,id)
                    }
                }
            }
    }


    override fun onRoomClick(room: RoomData) {

        interAd?.show(this)
        requestInterAd()


        val mNames: ArrayList<String> = ArrayList()
        val mUrl: ArrayList<String> = ArrayList()

        for (i in 0 until room.membersUrl.size) {
            var fl = 0
            for (j in mUrl) {
                if (room.membersUrl[i] == j) {
                    fl = 1
                }
            }
            if (fl == 0) {
                mNames.add(room.membersName[i])
                mUrl.add(room.membersUrl[i])
            }

        }
        Log.d(TAG, "FOr : Names : $mNames")
        val bundle = Bundle()
        bundle.putString("userId", userId)
        bundle.putString("roomName", room.roomName)
        bundle.putString("roomId", room.roomId)
        bundle.putString("rank", room.rank)
        bundle.putStringArrayList("members", room.members)
        bundle.putStringArrayList("membersName", mNames)
        bundle.putStringArrayList("membersUrl", mUrl)


        val fragment = RoomFragment()
        fragment.arguments = bundle

        for (fragment in supportFragmentManager.fragments) {
            if (fragment != null)
                supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        supportFragmentManager.beginTransaction().add(R.id.mainContainter, fragment)
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }


    override fun onTimeCardClick(text: String) {
        val bundleUsage = Bundle()
        bundleUsage.putString("totalTime", text)
        bundleUsage.putStringArrayList("apps", screenTimeViewModel.appsArray)
        bundleUsage.putStringArrayList("appsTime", screenTimeViewModel.appsArrayTime)





        val fragment = UsageStatsFragment()
        fragment.arguments = bundleUsage

        for (fragment in supportFragmentManager.fragments) {
            if (fragment != null)
                supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        supportFragmentManager.beginTransaction().add(R.id.mainContainter, fragment).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }


    private fun requestInterAd() {

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, getString(R.string.interAdId), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    interAd = interstitialAd
                    Log.i(TAG, "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.d(TAG, loadAdError.toString())
                    interAd = null
                }
            })

    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.signOut -> signOut()
            R.id.aboutApp -> showInfoDialog()
            R.id.help -> {
                val helpIntent = Intent(this, WelcomeActivity::class.java);
                helpIntent.putExtra("KEY", "yes");
                startActivity(helpIntent)
            }
            R.id.createRoom -> {
                createRoomFragment()
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
            android.R.id.home -> {
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment != null)
                        supportFragmentManager.beginTransaction().remove(fragment).commit()
                }
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
            R.id.privacyPolicy -> {
                val browserIntent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://sites.google.com/view/screentimerank/home")
                    )
                startActivity(browserIntent)
            }
            R.id.rateUs -> {
                val browserIntent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.thakurnitin2684.screentimerank")
                    )
                startActivity(browserIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createRoomFragment() {
        for (fragment in supportFragmentManager.fragments) {
            if (fragment != null)
                supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        val fragment = CreateRoomFragment()
        supportFragmentManager.beginTransaction().add(R.id.mainContainter, fragment).commit()
    }

    private fun signOut() {
        authViewModel.signOut()
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            // Google Sign In failed, update UI appropriately
            Log.w("MainActivity", "Signed out of google")
        }
        startActivity(Intent(this, GoogleSignInActivity::class.java))
    }

    override fun onBackPressed() {
        for (fragment in supportFragmentManager.fragments) {
            if (fragment != null)
                supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    fun removeAllFragments() {
        for (fragment in supportFragmentManager.fragments) {
            if (fragment != null)
                supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        finish()
        startActivity(intent)
    }


    private fun showInfoDialog() {
        aboutBinding = AboutBinding.inflate(layoutInflater)


        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)
        aboutDialog = builder.setView(aboutBinding.root).create()
        aboutDialog?.setCanceledOnTouchOutside(true)
        aboutBinding.aboutVersion.text = BuildConfig.VERSION_NAME
        aboutDialog?.show()
    }


}
