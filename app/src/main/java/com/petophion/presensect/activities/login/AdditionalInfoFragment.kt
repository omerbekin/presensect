package com.petophion.presensect.activities.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.petophion.presensect.R
import com.petophion.presensect.databases.UserInfoDb
import kotlinx.android.synthetic.main.fragment_additional_info.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AdditionalInfoFragment : Fragment() {

    private val DEFAULT_PROFILE_PICTURE_URL =
        "https://firebasestorage.googleapis.com/v0/b/presensect.appspot.com/o/profilePictures%2Fdefault_profile_picture.png?alt=media&token=222730a2-550f-46b0-930f-61ff56c35840"

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val bundle = this.arguments
        val newUserUID = bundle?.getString("newUserUID", "error")
        val username = bundle?.getString("username")
        val imageUrl = bundle?.getString("imageUrl", DEFAULT_PROFILE_PICTURE_URL)

        btnFinish.setOnClickListener {

            val first = etAddFirst.text.toString()
            val last = etAddLast.text.toString()
            val bio = etAddBio.text.toString()

            val profileInfoDb = UserInfoDb(newUserUID, username, first, last, bio, imageUrl)

            CoroutineScope(Dispatchers.IO).launch {
                uploadUserInfo(newUserUID!!, profileInfoDb)

                val fragmentManager = requireActivity().supportFragmentManager
                for (i in 0 until fragmentManager.backStackEntryCount) {
                    fragmentManager.popBackStack()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_additional_info, container, false)
    }

    private suspend fun uploadUserInfo(newUserUID: String, userInfoDb: UserInfoDb) {

        val database = Firebase.firestore
        val infoDataRef = database.collection("usersInfo")

        infoDataRef.document(newUserUID).set(userInfoDb).addOnSuccessListener {
            Toast.makeText(
                context, "All Done!",
                Toast.LENGTH_SHORT
            ).show()
        }.await()
    }
}