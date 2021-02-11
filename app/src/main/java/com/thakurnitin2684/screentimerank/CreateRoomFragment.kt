package com.thakurnitin2684.screentimerank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_create_room.view.*


class CreateRoomFragment : Fragment() {
    private lateinit var myDatabase: Database


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_create_room, container, false)
        root.createButton.setOnClickListener {
            myDatabase = Database()
            myDatabase.open()
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val roomName = root.nameEditText.editText?.text.toString()

            if (userId != null) {
                myDatabase.createNewRoom(userId, roomName)
                (activity as MainActivity).removeAllFragments()
            } else {
                Toast.makeText(requireContext(), "Room Not Created", Toast.LENGTH_SHORT).show()
            }
        }
        return root
    }

}
