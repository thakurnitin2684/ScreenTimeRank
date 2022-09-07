package com.thakurnitin2684.screentimerank.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thakurnitin2684.screentimerank.data.FirebaseProfileService
import com.thakurnitin2684.screentimerank.data.Room
import com.thakurnitin2684.screentimerank.data.RoomData
import com.thakurnitin2684.screentimerank.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProfileViewModel : ViewModel() {
    private val _userProfile = MutableLiveData<User>()
    val userProfile: LiveData<User> = _userProfile

    private val _roomDetails = MutableLiveData<MutableList<Room?>>()
    val roomDetails: LiveData<MutableList<Room?>> = _roomDetails


    private val _allUsers = MutableLiveData<MutableList<MutableList<User?>>>()
    val allUsers: LiveData<MutableList<MutableList<User?>>> = _allUsers




    fun getUserDetails(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _userProfile.postValue(FirebaseProfileService.getUserDetails(userId))
            }

        }
    }


    fun getAllUserDetails(data: MutableList<RoomData>) {

        viewModelScope.launch {

            withContext(Dispatchers.Default) {
                val allRoomUsersData : MutableList<MutableList<User?>> = mutableListOf()

                for (room in data) {
                    val usersData : MutableList<User?> = mutableListOf()

                    for(id in room.members){
                        val result: User? = FirebaseProfileService.getUserDetails(id)
                        usersData.add(result)
                    }

                    allRoomUsersData.add(usersData)
                }
                _allUsers.postValue(allRoomUsersData)

            }


        }


    }




    fun getRoomDetails(rooms: ArrayList<String>) {

        viewModelScope.launch {


            withContext(Dispatchers.Default) {
                val roomsData : MutableList<Room?> = mutableListOf()
                for (id in rooms) {

                    val result: Room? = FirebaseProfileService.getRoomDetails(id)
                    roomsData.add(result)

                }
                _roomDetails.postValue(roomsData)

            }


        }


    }

    fun addDbforFirstTime(userId: String, user: User) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                FirebaseProfileService.addDbForFirstTime(userId, user)
            }
        }
    }

    fun createNewRoom(userId: String, roomName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                FirebaseProfileService.createNewRoom(userId, roomName)
            }
        }
    }

    fun deleteRoom(userId: String, roomId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                FirebaseProfileService.deleteRoom(userId, roomId)
            }
        }
    }

    fun updateTime(userId: String?, time: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                FirebaseProfileService.updateTime(userId, time)
            }
        }
    }

    fun addForRoom(userId: String?, roomId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                FirebaseProfileService.addForRoom(userId, roomId)
            }
        }
    }

}