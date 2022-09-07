package com.thakurnitin2684.screentimerank.ui.adapter

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thakurnitin2684.screentimerank.databinding.EachAppBinding


class AppsAdapter(
    private val context: Context,
    private val appsPackages: ArrayList<String>,
    private val appsTimes: ArrayList<String>

) : RecyclerView.Adapter<AppsAdapter.DataViewHolder>() {


    private lateinit var binding: EachAppBinding


    class DataViewHolder(private val binding: EachAppBinding) :
        RecyclerView.ViewHolder(binding.root) {



        fun bind(appPackage: String, appsTimes: String, context: Context) {


            binding.appTime.text = appsTimes


            //App Name
            val pm: PackageManager = context.packageManager
            val appInfo: ApplicationInfo? = try {

                pm.getApplicationInfo(appPackage, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            val applicationName =
                (if (appInfo != null) pm.getApplicationLabel(appInfo) else appPackage)



            binding.appName.text = applicationName.toString()

            if (applicationName != appPackage) {
                binding.appIconLayout.setImageDrawable(pm.getApplicationIcon(appPackage))
            }
            binding.appPackage.text = appPackage


            binding.root.setOnClickListener{
                if ( binding.appPackage.visibility == View.VISIBLE){
                    binding.appPackage.visibility = View.GONE
                }else{
                    binding.appPackage.visibility = View.VISIBLE
                }
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        binding = EachAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataViewHolder(binding)

    }


    override fun getItemCount(): Int = appsPackages.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(appsPackages[position], appsTimes[position], context)
    }

    fun addData(
        packages: ArrayList<String>,
        times: ArrayList<String>
    ) {
        appsPackages.clear()
        appsTimes.clear()

        appsPackages.addAll(packages)
        appsTimes.addAll(times)

    }
}