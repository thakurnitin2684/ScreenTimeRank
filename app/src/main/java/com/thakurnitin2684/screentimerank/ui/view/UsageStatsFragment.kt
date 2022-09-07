package com.thakurnitin2684.screentimerank.ui.view

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.thakurnitin2684.screentimerank.R
import com.thakurnitin2684.screentimerank.databinding.FragmentUsageStatsBinding
import com.thakurnitin2684.screentimerank.ui.adapter.AppsAdapter


//Fragment that contains information about user stats, time used by each app
class UsageStatsFragment : Fragment() {

    private var _binding: FragmentUsageStatsBinding? = null
    private val binding get() = _binding!!

    private lateinit var appsAdapter: AppsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUsageStatsBinding.inflate(inflater, container, false)
        val view = binding.root



        //Retrieving the data passed through bundle
        val totalTime = requireArguments().getString("totalTime")
        val appsPackageList = requireArguments().getStringArrayList("apps")
        val appsTimeList = requireArguments().getStringArrayList("appsTime")



        //If list is not empty that set the adapter
        if(appsPackageList!=null && appsTimeList!=null) {

            //Adapter Inflation
            binding.appsList.layoutManager = LinearLayoutManager(requireActivity())
            appsAdapter = AppsAdapter(requireActivity().applicationContext, appsPackageList, appsTimeList)
            binding.appsList.addItemDecoration(
                DividerItemDecoration(
                    binding.appsList.context,
                    (binding.appsList.layoutManager as LinearLayoutManager).orientation
                )
            )
            binding.appsList.adapter = appsAdapter






        }
        binding.expandButton.setOnClickListener{
            if(binding.expandLayout.visibility== View.GONE){
                TransitionManager.beginDelayedTransition(
                    binding.root,
                    AutoTransition()
                )
                binding.expandLayout.visibility=View.VISIBLE
                binding.expandButton.setBackgroundResource(R.drawable.arrow_up)
            }else{
                TransitionManager.beginDelayedTransition(
                    binding.root,
                    AutoTransition()
                )
                binding.expandLayout.visibility=View.GONE
                binding.expandButton.setBackgroundResource(R.drawable.arrow_down)
            }
        }

        //To open system's section of usage stats
        binding.expandLayout.setOnClickListener{
            startActivity(Intent(Settings.ACTION_SETTINGS))

        }


        binding.timeTotal.text=totalTime

        return view
    }


}
