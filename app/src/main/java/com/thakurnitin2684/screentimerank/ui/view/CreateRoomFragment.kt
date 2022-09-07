package com.thakurnitin2684.screentimerank.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.thakurnitin2684.screentimerank.R
import com.thakurnitin2684.screentimerank.databinding.FragmentCreateRoomBinding
import com.thakurnitin2684.screentimerank.ui.viewmodel.AuthViewModel
import com.thakurnitin2684.screentimerank.ui.viewmodel.UserProfileViewModel


class CreateRoomFragment : Fragment() {


    private var _binding: FragmentCreateRoomBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private val userProfileViewModel: UserProfileViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCreateRoomBinding.inflate(inflater, container, false)
        val view = binding.root


        binding.createButton.setOnClickListener {


            val userId = authViewModel.getCurrentUser()?.uid
            val roomName = binding.nameEditText.editText?.text.toString()

            if(roomName.isNotEmpty()){
                if (userId != null) {
                    userProfileViewModel.createNewRoom(userId, roomName)
                    (activity as MainActivity).removeAllFragments()
                } else {
                    Toast.makeText(requireContext(), "Room Not Created", Toast.LENGTH_SHORT).show()
                }
            }

        }


        return view
    }

}
