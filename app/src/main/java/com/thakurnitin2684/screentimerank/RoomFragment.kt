package com.thakurnitin2684.screentimerank

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.android.synthetic.main.fragment_room.view.*
import java.util.*

private const val TAG="RoomFragment"
private const val DEEP_LINK_URL="https://play.google.com/store/apps/details?id=com.thakurnitin2684.screentimerank&data="
class RoomFragment : Fragment() {
    private lateinit var database: Database

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_room, container, false)
        val userId = arguments!!.getString("userId")
        val roomId = arguments!!.getString("roomId")
        val roomName = arguments!!.getString("roomName")
        val rank = arguments!!.getString("rank")
        val members = arguments!!.getStringArrayList("members")
        val membersName = arguments!!.getStringArrayList("membersName")
        val membersUrl = arguments!!.getStringArrayList("membersUrl")

        val newDeepLink = builDeepLink(Uri.parse(DEEP_LINK_URL+roomId))

        root.roomName.text = roomName
        root.rankNumber.text ="#"+rank
        root.outOfText.text = "out of "+members?.size
        root.membersNumber.text=members?.size.toString() +" member(s)"
        var n = rank?.toInt()?.minus(1)
        var t = members?.size

        root.whichMeans.text= getString(R.string.whichMeans,n ,t)

         if(membersName!=null && membersUrl!=null) {
             val dataAdapter =
                 MembersAdapter(requireContext(), R.layout.each_member, membersName, membersUrl)
             root.membersListView.layoutManager = LinearLayoutManager(context)
             root.membersListView.adapter = dataAdapter
         }
        var cal = Calendar.getInstance()
//        cal.set(Calendar.HOUR_OF_DAY, 0)
        val showString :String
        val currentHr = cal.get(Calendar.HOUR_OF_DAY)
        showString = if(currentHr%2!=0){
            (currentHr-1).toString()
        }else{
            currentHr.toString()
        }
        root.lastUpdated.text = getString(R.string.lastUpdtd,showString)

        database = Database()
        database.open()
        root.delteButton.setOnClickListener{
            if (userId != null && roomId != null) {
                    database.deleteRoom(userId, roomId)
                (activity as MainActivity).removeAllFragments()
            }
        }
        root.inviteButton.setOnClickListener{
            shareDeepLink(newDeepLink.toString())
        }
        return root
    }
    private fun builDeepLink(deepLink :Uri):Uri{
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(deepLink.toString()))
            .setDomainUriPrefix("https://screentimerank.page.link")
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            .buildDynamicLink()
        return dynamicLink.uri
    }
    private fun shareDeepLink(deepLink: String){
        val intent = Intent(Intent.ACTION_SEND)
        intent.type="text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT,"I invite you to join my room on Screen Time Rank,click the link below to join my room ")
        intent.putExtra(Intent.EXTRA_TEXT,deepLink)
        startActivity(intent)
    }

}
