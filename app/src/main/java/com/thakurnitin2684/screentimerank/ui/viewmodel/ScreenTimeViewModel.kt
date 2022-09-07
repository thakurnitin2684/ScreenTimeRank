package com.thakurnitin2684.screentimerank.ui.viewmodel

import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.thakurnitin2684.screentimerank.ui.view.AppUsageInfo
import java.util.*

class ScreenTimeViewModel(application: Application) : AndroidViewModel(application) {

    val appsArray: ArrayList<String> = ArrayList()
    val appsArrayTime: ArrayList<String> = ArrayList()
    var totalTime: Long = 0
    var totalScreenTime: Long = 0
    var hourInt: Int = 0

    fun screenTime() {
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
            getApplication<Application>().applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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

        val appsArrayTimeSort: ArrayList<Long> = ArrayList()

        smallInfoList.addAll(map.values)
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
        }


    }


    fun screenTimeDatabase() {
        //Screen Time Code for databse updation
        val timeNow = Calendar.getInstance()
        hourInt = timeNow.get(Calendar.HOUR_OF_DAY)
        if (hourInt % 2 != 0) {
            hourInt -= 1
        }

        val cal = Calendar.getInstance()
        val endCal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 1)

        endCal.set(Calendar.HOUR_OF_DAY, hourInt)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.SECOND, 2)
        val startTime2 = cal.timeInMillis
        var currentEvent2: UsageEvents.Event
        val allEvents2: MutableList<UsageEvents.Event> = java.util.ArrayList()
        val map2 = HashMap<String, AppUsageInfo?>()

        val mUsageStatsManager2 =
            getApplication<Application>().applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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

                totalScreenTime += appUsageInfo.timeInForeground

            }
        }


        //End of Screen Time Code for database updation
    }


    private fun getAppNameFromPackage(
        packageName: String
    ): String {

        val pm: PackageManager = getApplication<Application>().applicationContext.packageManager
        val ai: ApplicationInfo? = try {
            pm.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val applicationName =
            (if (ai != null) pm.getApplicationLabel(ai) else "(unknown)")
        return applicationName.toString()
    }
}

