package com.thakurnitin2684.screentimerank

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.transition.AutoTransition
import android.transition.TransitionManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.each_app.view.*
import kotlinx.android.synthetic.main.fragment_usage_stats.*
import kotlinx.android.synthetic.main.fragment_usage_stats.view.*


class UsageStatsFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root= inflater.inflate(R.layout.fragment_usage_stats, container, false)
        val totalTime = arguments!!.getString("totalTime")
        val appsPackageList = arguments!!.getStringArrayList("apps")
        val appsTimeList = arguments!!.getStringArrayList("appsTime")

        if(appsPackageList!=null && appsTimeList!=null) {
            val dataAdapter =
                AppsAdapter(requireContext(), R.layout.each_app, appsPackageList, appsTimeList)
            root.appsList.adapter = dataAdapter
        }
        root.expandButton.setOnClickListener{
            if(expandLayout.visibility== View.GONE){
                TransitionManager.beginDelayedTransition(
                    actionMainConstraint,
                    AutoTransition()
                )
                expandLayout.visibility=View.VISIBLE
                expandButton.setBackgroundResource(R.drawable.arrow_up)
            }else{
                TransitionManager.beginDelayedTransition(
                    actionMainConstraint,
                    AutoTransition()
                )
                expandLayout.visibility=View.GONE
                expandButton.setBackgroundResource(R.drawable.arrow_down)
            }
        }
        root.expandLayout.setOnClickListener{
            startActivity(Intent(Settings.ACTION_APP_USAGE_SETTINGS))

        }

        root.appsList.setOnItemClickListener { _, view, _, _ ->
            val packageName : TextView = view.findViewById(R.id.appPackage)
            if (packageName.visibility == View.VISIBLE){
                packageName.visibility = View.GONE
            }else{
                packageName.visibility = View.VISIBLE
            }
        }
        root.timeTotal.text=totalTime
        return root
    }


}
