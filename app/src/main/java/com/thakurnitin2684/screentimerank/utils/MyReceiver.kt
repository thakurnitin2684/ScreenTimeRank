package com.thakurnitin2684.screentimerank.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.USAGE_STATS_SERVICE
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.thakurnitin2684.screentimerank.ui.view.AppUsageInfo
import com.thakurnitin2684.screentimerank.ui.viewmodel.UserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject



@AndroidEntryPoint
class MyReceiver @Inject constructor(private val userProfileViewModel: UserProfileViewModel) : BroadcastReceiver() {

    private var userId: String? = null



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onReceive(context: Context?, intent: Intent?) {

         val   prefRepository by lazy { context?.let { PrefRepository(it.applicationContext) } }

        userId = FirebaseAuth.getInstance().currentUser?.uid



        val bl = prefRepository?.getUnivInt()
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
        val allEvents2: MutableList<UsageEvents.Event> = ArrayList()
        val map2 = HashMap<String, AppUsageInfo?>()

        val mUsageStatsManager2 =
            context?.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
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
        var smallInfoList2: MutableList<AppUsageInfo?> = ArrayList()

        smallInfoList2.addAll(map2.values)
        var totalTime2: Long = 0
        for (appUsageInfo in smallInfoList2) {
            if (appUsageInfo != null) {

                totalTime2 += appUsageInfo.timeInForeground

            }
        }


//        val hour = totalTime / 3600000
//        val min = (totalTime / 60000) % 60


        if (hr != bl) {
            userProfileViewModel.updateTime(userId,totalTime2)

            prefRepository?.setUnivInt(hr)

        }
        val cal2 = Calendar.getInstance()
        val intent = Intent(context, MyReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context, 2, intent, 0
        )
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        var hur = timeNow.get(Calendar.HOUR_OF_DAY)
        if (hur % 2 != 0) {
            cal2.add(Calendar.HOUR, 1)

        } else {
            cal2.add(Calendar.HOUR, 2)

        }
        cal2.set(Calendar.MINUTE, 0)
        cal2.set(Calendar.SECOND, 0)

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            if (Build.VERSION.SDK_INT < 23) {
                if (Build.VERSION.SDK_INT >= 19) {

                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        cal2.timeInMillis,
                        pendingIntent
                    );
                } else {

                    alarmManager.set(AlarmManager.RTC_WAKEUP, cal2.timeInMillis, pendingIntent);
                }
            } else {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal2.timeInMillis,
                    pendingIntent
                );
            }
        }

    }


}