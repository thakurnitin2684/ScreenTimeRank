package com.thakurnitin2684.screentimerank.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.NativeAd
import com.squareup.picasso.Picasso
import com.thakurnitin2684.screentimerank.utils.CircleTransform
import com.thakurnitin2684.screentimerank.data.RoomData
import com.thakurnitin2684.screentimerank.data.User
import com.thakurnitin2684.screentimerank.databinding.AdUnifiedBinding
import com.thakurnitin2684.screentimerank.databinding.EachRoomBinding
import com.thakurnitin2684.screentimerank.databinding.UserdataLayoutBinding


private const val TAG = "UserDataAdapter"

private const val TYPE_HEADER = 0
private const val TYPE_ITEM = 1
private const val TYPE_AD = 2


class UserDataAdapter(
    private val listener: OnTaskClickListener,
    private val rooms: MutableList<RoomData>,
    private var user: User?,
    private var totalTime: Long,
    private var adData: NativeAd?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var binding: EachRoomBinding
    private lateinit var userDataLayoutBinding: UserdataLayoutBinding
    private lateinit var adUnifiedBinding: AdUnifiedBinding


    interface OnTaskClickListener {

        fun onRoomClick(room: RoomData)
        fun onTimeCardClick(text: String)

    }


    private class HeaderViewHolder(private val userDataLayoutBinding: UserdataLayoutBinding) :
        RecyclerView.ViewHolder(userDataLayoutBinding.root) {

        fun bind(listener: OnTaskClickListener, user: User?, totalTime: Long) {
            userDataLayoutBinding.nameText.text = user?.name
            userDataLayoutBinding.emailText.text = user?.email
            Picasso.get().load(user?.url)
                .error(com.thakurnitin2684.screentimerank.R.drawable.profile)
                .placeholder(com.thakurnitin2684.screentimerank.R.drawable.profile)
                .transform(CircleTransform())
                .into(userDataLayoutBinding.imageUrlText)




            val hour = totalTime / 3600000
            val min = (totalTime / 60000) % 60

            if (hour == 0.toLong()) {

                userDataLayoutBinding.screenTimeText.text = min.toString() + "m"

            } else if (hour == 0.toLong() && min == 0.toLong()) {
                userDataLayoutBinding.screenTimeText.text = "<1m"
            } else {
                userDataLayoutBinding.screenTimeText.text = hour.toString() + "h " + min + "m"
            }

            userDataLayoutBinding.timeCard.setOnClickListener {

                listener.onTimeCardClick(userDataLayoutBinding.screenTimeText.text as String)

            }

        }

    }


    private class AdHolderView(private val adUnifiedBinding: AdUnifiedBinding) :
        RecyclerView.ViewHolder(adUnifiedBinding.root) {

        fun bind(adData: NativeAd?) {


            if (adData != null) {

                adUnifiedBinding.root.visibility=View.VISIBLE


              Log.d(TAG,"adData:::: $adData")


                adUnifiedBinding.adHeadline.text = adData.headline
                adUnifiedBinding.adBody.text = adData.body
                adUnifiedBinding.adCallToAction.text = adData.callToAction

                val icon: NativeAd.Image? = adData.icon

                if (icon != null) {
                    adUnifiedBinding.adIcon.setImageDrawable(icon.drawable)
                }

                adUnifiedBinding.adIcon.visibility = View.VISIBLE

                if (adData.price == null) {
                    adUnifiedBinding.adPrice.visibility = View.INVISIBLE
                } else {
                    adUnifiedBinding.adPrice.visibility = View.VISIBLE
                    adUnifiedBinding.adPrice.text = adData.price
                }

                if (adData.store == null) {
                    adUnifiedBinding.adStore.visibility = View.INVISIBLE
                } else {
                    adUnifiedBinding.adStore.visibility = View.VISIBLE
                    adUnifiedBinding.adStore.text = adData.store
                }

                if (adData.starRating == null) {
                    adUnifiedBinding.adStars.visibility = View.INVISIBLE
                } else {
                    adUnifiedBinding.adStars.rating = adData.starRating!!.toFloat()

                    adUnifiedBinding.adStars.visibility = View.VISIBLE
                }

                if (adData.advertiser == null) {
                    adUnifiedBinding.adAdvertiser.visibility = View.INVISIBLE
                } else {
                    adUnifiedBinding.adAdvertiser.text = adData.advertiser
                    adUnifiedBinding.adAdvertiser.visibility = View.VISIBLE
                }

                adUnifiedBinding.adView.mediaView = adUnifiedBinding.adMedia




               adUnifiedBinding.adView.setNativeAd(adData)


            }else{
                adUnifiedBinding.root.visibility=View.GONE
            }


        }


    }


    class DataViewHolder(private val binding: EachRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(room: RoomData, listener: OnTaskClickListener) {

            if(room.rank!="0"){
                binding.rankProgressBar.visibility=View.GONE
                binding.rank.visibility=View.VISIBLE

            }
            binding.roomName.text = room.roomName

            binding.rank.text = "#" + room.rank
            binding.outOf.text =
                "out of " + room.members.size.toString()

            binding.root.setOnClickListener {
                listener.onRoomClick(room)
            }


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_ITEM -> {

                binding =
                    EachRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DataViewHolder(binding)

            }
            TYPE_HEADER -> {

                userDataLayoutBinding = UserdataLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(userDataLayoutBinding)
            }
            TYPE_AD -> {

                adUnifiedBinding = AdUnifiedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AdHolderView(adUnifiedBinding)
            }

            else -> {
                binding =
                    EachRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DataViewHolder(binding)
            }
        }

    }


    override fun getItemViewType(position: Int): Int {

        if(adData!=null){
            return when (position) {
                0 -> {
                    TYPE_HEADER
                }
                3 -> {
                    TYPE_AD
                }
                else -> TYPE_ITEM
            }

        }else{
            return when (position) {
                0 -> {
                    TYPE_HEADER
                }

                else -> TYPE_ITEM
            }

        }


    }


    override fun getItemCount(): Int {
        return if(adData!=null){
            rooms.size + 2

        }else{
            rooms.size + 1

        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        when (holder) {
            is HeaderViewHolder -> {
                holder.bind(listener, user, totalTime)
            }
            is DataViewHolder -> {
                if(adData!=null){
                    if(position < 3){
                        holder.bind(rooms[position - 1], listener)

                    }else{
                        holder.bind(rooms[position - 2], listener)

                    }
                }else{
                    holder.bind(rooms[position - 1], listener)

                }

            }
            is AdHolderView -> {
                holder.bind(adData)

            }
        }

    }


    fun addData(
        rms: MutableList<RoomData>,
    ) {
        rooms.clear()

        rooms.addAll(rms)

    }

    fun addHeaderData(
        u: User
    ) {

        user = u

    }

    fun addHeaderTime(
        time: Long
    ) {
        totalTime = time

    }

    fun addAdData(
        ad: NativeAd
    ) {
        adData = ad
    }


}
