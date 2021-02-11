package com.thakurnitin2684.screentimerank

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.ads.formats.NativeAd
import com.google.firebase.firestore.FirebaseFirestore


private const val TAG = "UserDataAdap"


class RoomData {
    lateinit var roomId: String
    lateinit var roomName: String
    lateinit var rank: String
    lateinit var members: ArrayList<String>
    var membersName: ArrayList<String> = ArrayList()
    var membersUrl: ArrayList<String> = ArrayList()
}

class ViewHolder(v: View) {
    val name: TextView = v.findViewById(R.id.roomName)
    val rank: TextView = v.findViewById(R.id.rank)
    val outOfVar: TextView = v.findViewById(R.id.outOf)

}

class UserDataAdapter(
    context: Context,
    private val resource: Int,
    private val rooms: MutableList<RoomData>,
    private val data : MutableList<RoomData>
) : ArrayAdapter<RoomData>(context, resource) {



    private var myDatabase: FirebaseFirestore? = null
    private lateinit var database: Database
    private val inflater = LayoutInflater.from(context)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        if (position == positionOfAd && nAd != null && positionOfAd>0) {
            val view: View = inflater.inflate(R.layout.ad_unified, parent, false)
           val  holder  = UnifiedNativeAdViewHolder(view)
            view.tag = "7FbVmXeNNDSqFu2vZhfp"

            val adView = holder.adView

                  // and if so, insert the ads into the list.
                    val nativeAd = nAd
            if (nativeAd != null) {
                Log.d(TAG, "@@@AdLoaded : ${nativeAd.headline}")

                (adView.headlineView as TextView).text = nativeAd.headline
                (adView.bodyView as TextView).text = nativeAd.body
                (adView.callToActionView as Button).text = nativeAd.callToAction

                val icon: NativeAd.Image? = nativeAd.icon

                if (icon != null) {
                    (adView.iconView as ImageView).setImageDrawable(icon.drawable)
                }
                adView.iconView.visibility = View.VISIBLE

                if (nativeAd.price == null) {
                    adView.priceView.visibility = View.INVISIBLE
                } else {
                    adView.priceView.visibility = View.VISIBLE
                    (adView.priceView as TextView).text = nativeAd.price
                }

                if (nativeAd.store == null) {
                    adView.storeView.visibility = View.INVISIBLE
                } else {
                    adView.storeView.visibility = View.VISIBLE
                    (adView.storeView as TextView).text = nativeAd.store
                }

                if (nativeAd.starRating == null) {
                    adView.starRatingView.visibility = View.INVISIBLE
                } else {
                    (adView.starRatingView as RatingBar).rating =
                        nativeAd.starRating.toFloat()
                    adView.starRatingView.visibility = View.VISIBLE
                }

                if (nativeAd.advertiser == null) {
                    adView.advertiserView.visibility = View.INVISIBLE
                } else {
                    (adView.advertiserView as TextView).text = nativeAd.advertiser
                    adView.advertiserView.visibility = View.VISIBLE
                }

                adView.setNativeAd(nativeAd)


            }

            return view

        } else {
            var view: View
            val viewHolder: ViewHolder

            if (convertView == null) {
                view = inflater.inflate(resource, parent, false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            } else {
                view = convertView
                if (view.tag == "7FbVmXeNNDSqFu2vZhfp") {
                    view = inflater.inflate(resource, parent, false)
                    viewHolder = ViewHolder(view)
                    view.tag = viewHolder
                } else {
                    viewHolder = view.tag as ViewHolder
                }
            }


            val currentRoom = rooms[position]
            data[position].roomId = currentRoom.roomId
            Log.d(TAG,"Position :$position")
               val nm = currentRoom.roomName
                data[position].roomName = nm
                viewHolder.name.text = currentRoom.roomName
                data[position].members = currentRoom.members

                 data[position].rank =currentRoom.rank
                viewHolder.rank.text ="#"+currentRoom.rank
                viewHolder.outOfVar.text =
                                "out of " + data[position].members.size.toString()
               data[position].membersName = currentRoom.membersName
              data[position].membersUrl = currentRoom.membersUrl
            return view
        }
    }

    override fun getCount(): Int {
        return rooms.size
    }

    override fun getItem(position: Int): RoomData? {
        return data[position]
    }

}
