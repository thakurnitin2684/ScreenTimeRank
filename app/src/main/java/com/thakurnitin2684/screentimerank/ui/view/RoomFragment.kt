package com.thakurnitin2684.screentimerank.ui.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.thakurnitin2684.screentimerank.R
import com.thakurnitin2684.screentimerank.databinding.FragmentRoomBinding
import com.thakurnitin2684.screentimerank.ui.adapter.MembersAdapter
import com.thakurnitin2684.screentimerank.ui.viewmodel.UserProfileViewModel
import java.util.*

private const val TAG="RoomFragment"
private const val DEEP_LINK_URL="https://play.google.com/store/apps/details?id=com.thakurnitin2684.screentimerank&data="
class RoomFragment : Fragment() {



    private var _binding: FragmentRoomBinding? = null
    private val binding get() = _binding!!


    lateinit var membersAdapter: MembersAdapter

    private val userProfileViewModel: UserProfileViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentRoomBinding.inflate(inflater, container, false)
        val view = binding.root

        val userId = requireArguments().getString("userId")
        val roomId = requireArguments().getString("roomId")
        val roomName = requireArguments().getString("roomName")
        val rank = requireArguments().getString("rank")
        val members = requireArguments().getStringArrayList("members")
        val membersName = requireArguments().getStringArrayList("membersName")
        val membersUrl = requireArguments().getStringArrayList("membersUrl")

        val newDeepLink = builDeepLink(Uri.parse(DEEP_LINK_URL+roomId))

        binding.roomName.text = roomName
        binding.rankNumber.text ="#"+rank
        binding.outOfText.text = "out of "+members?.size
        binding.membersNumber.text=members?.size.toString() +" member(s)"

        val n = rank?.toInt()?.minus(1)
        val t = members?.size

        binding.whichMeans.text= getString(R.string.whichMeans,n ,t)

         if(membersName!=null && membersUrl!=null) {
             //Adapter Inflation
             binding.membersListView.layoutManager = LinearLayoutManager(requireActivity())
             membersAdapter = MembersAdapter( membersName, membersUrl)
             binding.membersListView.addItemDecoration(
                 DividerItemDecoration(
                     binding.membersListView.context,
                     (binding.membersListView.layoutManager as LinearLayoutManager).orientation
                 )
             )
             binding.membersListView.adapter = membersAdapter
         }


        val cal = Calendar.getInstance()

        val showString :String
        val currentHr = cal.get(Calendar.HOUR_OF_DAY)
        showString = if(currentHr%2!=0){
            (currentHr-1).toString()
        }else{
            currentHr.toString()
        }
        binding.lastUpdated.text = getString(R.string.lastUpdtd,showString)


        binding.delteButton.setOnClickListener{
            if (userId != null && roomId != null) {
                    userProfileViewModel.deleteRoom(userId, roomId)
                (activity as MainActivity).removeAllFragments()
            }
        }
        binding.inviteButton.setOnClickListener{
            shareDeepLink(newDeepLink.toString())
        }
        return view
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
