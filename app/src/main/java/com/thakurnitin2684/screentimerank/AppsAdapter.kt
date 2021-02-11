package com.thakurnitin2684.screentimerank

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import java.util.ArrayList

class AppsViewHolder(v: View) {
    val appName: TextView = v.findViewById(R.id.appName)
    var appIcon: ImageView = v.findViewById(R.id.appIconLayout)
    val appPackage: TextView = v.findViewById(R.id.appPackage)
    val appTime: TextView = v.findViewById(R.id.appTime)
}

class AppsAdapter(
    context: Context,
    private val resource: Int,
    private val appsPackages: ArrayList<String>,
    private val appsTimes: ArrayList<String>

) : ArrayAdapter<String>(context, resource) {

    private val inflater = LayoutInflater.from(context)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View
        val viewHolder: AppsViewHolder

        if (convertView == null) {
            view = inflater.inflate(resource, parent, false)
            viewHolder = AppsViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as AppsViewHolder
        }
        val thisPackage = appsPackages[position]
        viewHolder.appTime.text = appsTimes[position]
        viewHolder.appName.text = getAppNameFromPackage(thisPackage)
        if (getAppNameFromPackage(thisPackage) != "unknown(Uninstalled)") {
            viewHolder.appIcon.setImageDrawable(getAppIcon(thisPackage))
        }
        viewHolder.appPackage.text = thisPackage

        return view
    }

    override fun getCount(): Int {
        return appsPackages.size
    }


    private fun getAppNameFromPackage(
        packageName: String
    ): String? {

        val pm: PackageManager = context.packageManager
        val ai: ApplicationInfo?
        ai = try {
            pm.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        val applicationName =
            (if (ai != null) pm.getApplicationLabel(ai) else "unknown(Uninstalled)")
        return applicationName.toString()
    }

    private fun getAppIcon(
        packageName: String
    ): Drawable? {
        val pm: PackageManager = context.packageManager
        return pm.getApplicationIcon(packageName)
    }
}