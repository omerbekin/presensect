package com.petophion.presensect.activities.login

import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.petophion.presensect.R
import com.petophion.presensect.databases.UserInfoDb
import kotlinx.android.synthetic.main.fragment_create_user.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*


class CreateUserFragment : Fragment() {

    private val auth = Firebase.auth
    private val database = Firebase.firestore

    private val DEFAULT_PROFILE_PICTURE_URL = "https://firebasestorage.googleapis.com/v0/b/presensect.appspot.com/o/profilePictures%2Fdefault_profile_picture.png?alt=media&token=222730a2-550f-46b0-930f-61ff56c35840"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_create_user, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        etAddUsername.filters = arrayOf<InputFilter>(
            object : AllCaps() {
                override fun filter(
                    source: CharSequence,
                    start: Int,
                    end: Int,
                    dest: Spanned,
                    dstart: Int,
                    dend: Int,
                ): CharSequence {
                    return source.toString().toLowerCase(Locale.ROOT)
                }
            }
        )

        btnCreateProfile.setOnClickListener {

            val username = etAddUsername.text.toString()
            val email = etAddEmail.text.toString()
            val password = etAddPassword.text.toString()

            val isUsernameLengthValid = username.length >= 4
            val isPasswordLengthValid = password.length >= 6

            if (username == "" || email == "" || password == "") {
                Toast.makeText(context, "Please don't leave any info blank", Toast.LENGTH_LONG)
                    .show()
            } else if (!isUsernameLengthValid) {
                Toast.makeText(context, "Username must be at least 4 characters", Toast.LENGTH_LONG)
                    .show()
            } else if (!isPasswordLengthValid) {
                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_LONG)
                    .show()
            } else if (email != "" && password != "" && password != "") {
                CoroutineScope(Dispatchers.IO).launch {
                    checkUniqueUsername(username, email, password)
                }
            }
        }
    }

    private suspend fun checkUniqueUsername(username: String, email: String, password: String) {

        val collectionPath = ("usersInfo")
        val infoDataRef = database.collection(collectionPath)
            .whereEqualTo("username", username)

        infoDataRef.get().addOnSuccessListener { documents ->
            var isUsernameExist = false
            for (document in documents) {
                isUsernameExist = true
//                break
            }

            if (!isUsernameExist) {
                CoroutineScope(Dispatchers.IO).launch {
                    createUser(email, password, username)
                }
            } else {
                Toast.makeText(context, "This username is taken", Toast.LENGTH_SHORT).show()
            }
        }.await()
    }

    private suspend fun createUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                CoroutineScope(Dispatchers.IO).launch {
                    setUsername(authResult, username)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    context, "Sign up Failed! ${it.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private suspend fun setUsername(authResult: AuthResult, username: String) {

        val newUserUID = authResult.user?.uid
        val collectionPath = ("usersInfo")
        val infoDataRef = database.collection(collectionPath)
        val profileInfoDb = UserInfoDb(newUserUID,  username, "", "", "", DEFAULT_PROFILE_PICTURE_URL)

        infoDataRef.document(newUserUID!!).set(profileInfoDb).addOnSuccessListener {
            Toast.makeText(
                context, "Sign up Successful",
                Toast.LENGTH_SHORT
            ).show()

            val addProfilePictureFragment = AddProfilePictureFragment()
            val bundle = Bundle()
            addProfilePictureFragment.arguments = bundle

            bundle.putString("newUserUID", newUserUID)
            bundle.putString("username", username)

            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.clCreateUserContainer, addProfilePictureFragment)
            transaction.commit()
            transaction.disallowAddToBackStack()

        }.await()
    }
}