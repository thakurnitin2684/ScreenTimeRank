package com.thakurnitin2684.screentimerank

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.transition.AutoTransition
import android.transition.TransitionManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_usage_stats.*
import kotlinx.android.synthetic.main.fragment_usage_stats.view.*


//Fragment that contains information about user stats, time used by each app
class UsageStatsFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root= inflater.inflate(R.layout.fragment_usage_stats, container, false)

        //Retrieving the data passed through bundle
        val totalTime = arguments!!.getString("totalTime")
        val appsPackageList = arguments!!.getStringArrayList("apps")
        val appsTimeList = arguments!!.getStringArrayList("appsTime")

        //If list is not empty that set the adapter
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

        //To open system's section of usage stats
        root.expandLayout.setOnClickListener{
            startActivity(Intent(Settings.ACTION_APP_USAGE_SETTINGS))

        }

        //Click listener for each item of list
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
