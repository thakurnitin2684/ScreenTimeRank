package com.thakurnitin2684.screentimerank

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

class AlarmDatabase(var context: Context) {

    fun setAlarmManager() {
        val intent = Intent(context, MyReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context, 2, intent, 0
        )
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager != null) {
            val cal = Calendar.getInstance()
            val hr = cal.get(Calendar.HOUR_OF_DAY)
            if (hr % 2 != 0) {
                cal.add(Calendar.HOUR, 1)

            } else {
                cal.add(Calendar.HOUR, 2)
            }
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)

            if (Build.VERSION.SDK_INT < 23) {

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            } else {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    pendingIntent
                )
            }

        }
    }

    fun cancelAlarmManager() {
        val intent = Intent(context, MyReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context, 2, intent, 0
        )
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent)
        }

    }
}