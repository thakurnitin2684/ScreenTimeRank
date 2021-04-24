package com.thakurnitin2684.screentimerank

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.userdata_layout.*
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "MainActivity"
var nAd: UnifiedNativeAd? = null
var positionOfAd = 0
const val UNIVE_INT: Int = 0
var totalTime2: Long = 0
class AppUsageInfo internal constructor(var packageName: String) {
    var timeInForeground: Long = 0
    var launchCount = 0
}

class MainActivity : AppCompatActivity() {

    private var myDatabase: FirebaseFirestore? = null
    private var userId: String? = null
    private var mAuth: FirebaseAuth? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var database: Database
    private var alarmHandler = AlarmDatabase(this)
    private lateinit var interAd: InterstitialAd
    private var aboutDialog: AlertDialog? = null


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        userId = FirebaseAuth.getInstance().currentUser?.uid



        //For alarm manager
        alarmHandler.cancelAlarmManager()
        alarmHandler.setAlarmManager()


        //Ads
        MobileAds.initialize(this, "ca-app-pub-9091905143657596~4053741257")
        interAd = InterstitialAd(this)
        interAd.adUnitId = getString(R.string.interAdId)


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



        //DataBase Read
        database = Database()
        database.open()
        myDatabase = FirebaseFirestore.getInstance()

        val docRef = myDatabase?.collection("users")?.document(
            userId!!
        )
         // READ CODE START
        docRef?.addSnapshotListener { documentSnapshot, e ->

            val inAnimation = AlphaAnimation(0f, 1f)
            inAnimation.duration = 200
            progressBarHolder.animation = inAnimation
            progressBarHolder.visibility = View.VISIBLE


            if (e != null) {
                Log.d(TAG, "Listen Failed", e)
                return@addSnapshotListener
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(User::class.java)
                if (user != null) {

                    nameText.text = user.name
                    emailText.text = user.email
                    Picasso.get().load(user.url)
                        .error(R.drawable.profile)
                        .placeholder(R.drawable.profile)
                        .transform(CircleTransform())
                        .into(imageUrlText)

                    val data: MutableList<RoomData> =
                        MutableList(user.rooms.size + 1) { RoomData() }
                    if (user.rooms.size < 3) {
                        positionOfAd = user.rooms.size - 1
                    } else {
                        positionOfAd = 2
                    }

                    val sendData: MutableList<RoomData> =
                        MutableList(user.rooms.size) { RoomData() }

                    var screenRList = 0
                    if(user.rooms.size==0){
                        var outAnimation = AlphaAnimation(1f, 0f)
                        outAnimation.duration = 200
                        progressBarHolder.animation = outAnimation
                        progressBarHolder.visibility = View.GONE
                    }
                    //Here is new logic
                    for (position in 0 until user.rooms.size) {
                        val currentRoom = user.rooms[position]
                        sendData[position].roomId = currentRoom
                        myDatabase = FirebaseFirestore.getInstance()
                        val docRef = myDatabase?.collection("rooms")?.document(
                            currentRoom
                        )
                        val screenTList: MutableList<Long> = ArrayList()
                        val docRef2 = myDatabase?.collection("users")


                        docRef?.get()?.addOnSuccessListener { documentSnapshot ->


                            Log.d(TAG, "Current Room Id :$currentRoom  Started")


                            sendData[position].roomName = (documentSnapshot.get("name")).toString()
                            sendData[position].members =
                                documentSnapshot.get("members") as ArrayList<String>


                            for (i in 0 until sendData[position].members.size) {
                                val eachRef = docRef2?.document(sendData[position].members[i])
                                eachRef?.get()?.addOnSuccessListener { documentSnapshot ->


                                    screenTList.add(documentSnapshot.get("screenTime") as Long)
                                    sendData[position].membersName.add(documentSnapshot.get("name") as String)
                                    sendData[position].membersUrl.add(documentSnapshot.get("url") as String)
                                    Log.d(TAG, "Screen Time list : $screenTList  at i : $i")

                                    if (screenTList.size == sendData[position].members.size) {

                                        screenTList.sort()
                                        val cRank =
                                            (screenTList.binarySearch(user.screenTime) + 1)
                                        sendData[position].rank =
                                            (sendData[position].members.size - cRank + 1).toString()
                                        Log.d(
                                            TAG,
                                            "#################Rank : ${sendData[position].rank}"
                                        )
                                        screenRList += 1

                                        if (screenRList == user.rooms.size) {
                                            //*************************************
                                            val dataAdapter = UserDataAdapter(
                                                this,
                                                R.layout.each_room,
                                                sendData,
                                                data
                                            )
                                            mainListView.adapter = dataAdapter
                                            var adLoader: AdLoader?


                                            //%%%%%%%%%%%%%%%%%%%
                                            var outAnimation = AlphaAnimation(1f, 0f)
                                            outAnimation.duration = 200
                                            progressBarHolder.animation = outAnimation
                                            progressBarHolder.visibility = View.GONE
                                            //%%%%%%%%%%%%%%%%%%%%%%
                                            val builder = AdLoader.Builder(
                                                this,
                                                getString(R.string.nativeAdId)
                                            )
                                            adLoader =
                                                builder.forUnifiedNativeAd { unifiedNativeAd -> // A native ad loaded successfully, check if the ad loader has finished loading
                                                    // and if so, insert the ads into the list.
                                                    Log.d(
                                                        TAG,
                                                        "@@@ Ad is loaded :${unifiedNativeAd}"
                                                    )
                                                    if (positionOfAd > 0) {
                                                        sendData.add(
                                                            positionOfAd,
                                                            sendData[positionOfAd]
                                                        )
                                                        nAd = unifiedNativeAd
                                                        dataAdapter.notifyDataSetChanged()
                                                    }
//


                                                }.withAdListener(
                                                    object : AdListener() {
                                                        override fun onAdFailedToLoad(errorCode: Int) {
                                                            // A native ad failed to load, check if the ad loader has finished loading
                                                            // and if so, insert the ads into the list.
                                                            Log.d(TAG, "@@@ Ad load Failed")
                                                        }
                                                    })
                                                    .build()
                                            adLoader.loadAd(AdRequest.Builder().build())
                                            /* ********************************************** */
                                        }


                                    }
                                }?.addOnFailureListener {
                                    Log.d(TAG, "User:")
                                }
                            }


                        }?.addOnFailureListener {
                            Log.d(TAG, "User:")
                        }
                    }


                    //Till here


                }
            }
        }

        //READ CODE ENDS


        //INFLATE heade vire
        val inflater = LayoutInflater.from(this)
        val headerView = inflater.inflate(R.layout.userdata_layout, mainListView, false)
        mainListView.addHeaderView(headerView)


        //Card Click Listener
        expandMainButton.setOnClickListener {
            if (detailsText.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(
                    mainListView,
                    AutoTransition()
                )
                detailsText.visibility = View.VISIBLE
                expandMainButton.setBackgroundResource(R.drawable.arrow_up)
            } else {
                TransitionManager.beginDelayedTransition(
                    mainListView,
                    AutoTransition()
                )
                detailsText.visibility = View.GONE
                expandMainButton.setBackgroundResource(R.drawable.arrow_down)
            }
        }


        //Screen Time code for UI
        val calStrt = Calendar.getInstance()
        calStrt.set(Calendar.HOUR_OF_DAY, 0)
        calStrt.set(Calendar.MINUTE, 0)
        calStrt.set(Calendar.SECOND, 1)
        val endTime = System.currentTimeMillis()
        val startTime = calStrt.timeInMillis
        var currentEvent: UsageEvents.Event
        val allEvents: MutableList<UsageEvents.Event> = ArrayList()
        val map = HashMap<String, AppUsageInfo?>()

        val mUsageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageEvents =
            mUsageStatsManager.queryEvents(startTime, endTime)
        while (usageEvents.hasNextEvent()) {
            currentEvent = UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED
            ) {
                allEvents.add(currentEvent)
                val key = currentEvent.packageName
                if (map[key] == null) map[key] = AppUsageInfo(key)
            }
        }
        for (i in 0 until allEvents.size - 1) {
            val event1 = allEvents[i]
            val event2 = allEvents[i + 1]
            if (event1.packageName != event2.packageName && event2.eventType == 1) {
                map[event2.packageName]!!.launchCount++
            }
            if (event1.eventType == 1 && event2.eventType == 2 && event1.className == event2.className
            ) {
                val diff = event2.timeStamp - event1.timeStamp
                map[event1.packageName]!!.timeInForeground += diff
            }
        }
        val smallInfoList: MutableList<AppUsageInfo?> = ArrayList()
        val appsArray: ArrayList<String> = ArrayList()
        val appsArrayTime: ArrayList<String> = ArrayList()
        val appsArrayTimeSort: ArrayList<Long> = ArrayList()

        smallInfoList.addAll(map.values)
        var totalTime: Long = 0
        var showString = "\n"
        for (appUsageInfo in smallInfoList) {
            if (appUsageInfo != null) {

                showString += (getAppNameFromPackage(appUsageInfo.packageName) + " : " + appUsageInfo.timeInForeground / 60000 + "\n")
                totalTime += appUsageInfo.timeInForeground
                appsArray.add(appUsageInfo.packageName)
                appsArrayTimeSort.add(appUsageInfo.timeInForeground / 60000)
                val hour = appUsageInfo.timeInForeground / 3600000
                val min = (appUsageInfo.timeInForeground / 60000) % 60

                if (hour == 0.toLong() && min != 0.toLong()) {
                    val time = min.toString() + "m"
                    appsArrayTime.add(time)
                } else if (hour == 0.toLong() && min == 0.toLong()) {
                    val time = "<1m"
                    appsArrayTime.add(time)
                } else {
                    val time = hour.toString() + "h " + min + "m"
                    appsArrayTime.add(time)
                }
            }
        }
        for (i in 0 until appsArrayTimeSort.size) {
            for (j in i until appsArrayTimeSort.size) {
                if (appsArrayTimeSort[i] < appsArrayTimeSort[j]) {
                    val temp = appsArrayTimeSort[i]
                    val temp3 = appsArrayTime[i]
                    val temp2 = appsArray[i]
                    appsArray[i] = appsArray[j]
                    appsArrayTimeSort[i] = appsArrayTimeSort[j]
                    appsArrayTime[i] = appsArrayTime[j]
                    appsArray[j] = temp2
                    appsArrayTimeSort[j] = temp
                    appsArrayTime[j] = temp3

                }
            }
            Log.d(TAG, "Time loop ${appsArrayTime[i]}")
        }


        Log.d(TAG, " Time : $showString")
        val hour = totalTime / 3600000
        val min = (totalTime / 60000) % 60
        if (hour == 0.toLong()) {
            screenTimeText.text = min.toString() + "m"

        } else if (hour == 0.toLong() && min == 0.toLong()) {
            screenTimeText.text = "<1m"
        } else {
            screenTimeText.text = hour.toString() + "h " + min + "m"
        }
        Log.d(TAG, " TotalTime : ${screenTimeText.text}")

       //ScreenTime Code for UI Ends

        //Screen Time Code for databse updation
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val bl = sharedPref.getInt(UNIVE_INT.toString(), -1)
        val timeNow = Calendar.getInstance()
        var hr = timeNow.get(Calendar.HOUR_OF_DAY)
        if (hr % 2 != 0) {
            hr -= 1
        }

        val cal = Calendar.getInstance()
        val endCal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 1)

        endCal.set(Calendar.HOUR_OF_DAY, hr)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.SECOND, 2)
        val startTime2 = cal.timeInMillis
        var currentEvent2: UsageEvents.Event
        val allEvents2: MutableList<UsageEvents.Event> = java.util.ArrayList()
        val map2 = HashMap<String, AppUsageInfo?>()

        val mUsageStatsManager2 =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageEvents2 =
            mUsageStatsManager2.queryEvents(startTime2, endCal.timeInMillis)
        while (usageEvents2.hasNextEvent()) {
            currentEvent2 = UsageEvents.Event()
            usageEvents2.getNextEvent(currentEvent2)
            if (currentEvent2.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                currentEvent2.eventType == UsageEvents.Event.ACTIVITY_PAUSED
            ) {
                allEvents2.add(currentEvent2)
                val key = currentEvent2.packageName
                if (map2[key] == null) map2[key] = AppUsageInfo(key)
            }
        }
        for (i in 0 until allEvents2.size - 1) {
            val event1 = allEvents2[i]
            val event2 = allEvents2[i + 1]

            if (event1.eventType == 1 && event2.eventType == 2 && event1.className == event2.className
            ) {
                val diff = event2.timeStamp - event1.timeStamp
                map2[event1.packageName]!!.timeInForeground += diff
            }
        }
        val smallInfoList2: MutableList<AppUsageInfo?> = java.util.ArrayList()

        smallInfoList2.addAll(map2.values)

        for (appUsageInfo in smallInfoList2) {
            if (appUsageInfo != null) {

                totalTime2 += appUsageInfo.timeInForeground

            }
        }
      Log.d(TAG,"FIRST BOOL IS : ${frstUpldBool}")
      if(frstUpldBool){

          database.updateTime(userId, totalTime2)
          Log.d(TAG,"TIME UPLOADED : ${totalTime2}")

      }

        if (hr != bl) {
            database.updateTime(userId, totalTime2)

            val editable = sharedPref.edit()
            editable.putInt(UNIVE_INT.toString(), hr)
            editable.apply()

        }
        //End of Screen Time Code for database updation


        timeCard.setOnClickListener {
            val bundleUsage = Bundle()
            bundleUsage.putString("totalTime", screenTimeText.text as String)
            bundleUsage.putStringArrayList("apps", appsArray)
            bundleUsage.putStringArrayList("appsTime", appsArrayTime)


            val fragment = UsageStatsFragment()
            fragment.arguments = bundleUsage

            for (fragment in supportFragmentManager.fragments) {
                if (fragment != null)
                    supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
            supportFragmentManager.beginTransaction().add(R.id.mainContainter, fragment).commit()
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        requestInterAd()
        mainListView.setOnItemClickListener { parent, _, position, _ ->
            if (position != 0) {
                if (interAd.isLoaded) {
                    interAd.show()


                }
                requestInterAd()
                val element = parent.getItemAtPosition(position) as RoomData
                Log.d(TAG, "RoomData: ${element.rank} ${element.roomId} ${element.membersName}")
                Log.d(TAG, "FOr : Names : ${element.membersName}")

                val mNames: ArrayList<String> = ArrayList()
                val mUrl: ArrayList<String> = ArrayList()

                for (i in 0 until element.membersUrl.size) {
                    var fl = 0
                    for (j in mUrl) {
                        if (element.membersUrl[i] == j) {
                            fl = 1
                        }
                    }
                    if (fl == 0) {
                        mNames.add(element.membersName[i])
                        mUrl.add(element.membersUrl[i])
                    }

                }
                Log.d(TAG, "FOr : Names : $mNames")
                val bundle = Bundle()
                bundle.putString("userId", userId)
                bundle.putString("roomName", element.roomName)
                bundle.putString("roomId", element.roomId)
                bundle.putString("rank", element.rank)
                bundle.putStringArrayList("members", element.members)
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
        }
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mAuth = FirebaseAuth.getInstance()

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
                        database.addForRoom(userId, id)
                    }
                }
            }
    }

    private fun requestInterAd() {
        val iadRqst = AdRequest.Builder().build()
        interAd.loadAd(iadRqst)
    }

    private fun getAppNameFromPackage(
        packageName: String
    ): String? {

        val pm: PackageManager = applicationContext.packageManager
        val ai: ApplicationInfo?
        ai = try {
            pm.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val applicationName =
            (if (ai != null) pm.getApplicationLabel(ai) else "(unknown)")
        return applicationName.toString()
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
                removeAllFragments()
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
        mAuth!!.signOut()

        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            // Google Sign In failed, update UI appropriately
            Log.w("MainActivity", "Signed out of google")

        }
        startActivity(Intent(this, GoogleSignInActivity::class.java))
    }

    override fun onBackPressed() {
        removeAllFragments()
    }

    fun removeAllFragments() {

        for (fragment in supportFragmentManager.fragments) {
            if (fragment != null)
                supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }


    private fun showInfoDialog() {
        val messgView = layoutInflater.inflate(R.layout.about, null, false)


        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)
        aboutDialog = builder.setView(messgView).create()
        aboutDialog?.setCanceledOnTouchOutside(true)
        val aboutVersion = messgView.findViewById(R.id.about_version) as TextView
        aboutVersion.text = BuildConfig.VERSION_NAME
        aboutDialog?.show()
    }

}
