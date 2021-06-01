package com.petophion.presensect.activities.main.bottomnavbar.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.petophion.presensect.R
import com.petophion.presensect.databases.UserInfoDb
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class EditProfileFragment : Fragment() {

    private var isProfilePictureChanged = false

    private val database = Firebase.firestore

    lateinit var imageUri: Uri
    lateinit var profilePictureUrl: String

    var infoList = ArrayList<UserInfoDb>()

    data class UsersPostDb(val documentUID: String = "")

    var usersPostList = ArrayList<UsersPostDb>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        ivEditProfilePicture.clipToOutline = true
        etFirstName.clipToOutline = true
        etLastName.clipToOutline = true
        etBio.clipToOutline = true

        pbEditImageUpload.visibility = View.INVISIBLE

        getCurrentProfileInfo()

        fabEditProfilePicture.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 0)
        }

        ivDecline.setOnClickListener {
            activity?.onBackPressed()
        }

        btnApply.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                uploadProfileDataToFirestore()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 0 && data != null) {
            imageUri = data.data!!
            ivEditProfilePicture.setImageURI(imageUri)
            isProfilePictureChanged = true
        }
    }

    private suspend fun getEditedData(): UserInfoDb? {

        val currentUserUID = Firebase.auth.currentUser!!.uid
        val username = tvEditUsername.text.toString()
        val first = etFirstName.text.toString()
        val last = etLastName.text.toString()
        val bio = etBio.text.toString()

        if (!isProfilePictureChanged) {
            //take old url
            return UserInfoDb(currentUserUID, username, first, last, bio, profilePictureUrl)
        } else if (isProfilePictureChanged) {
            //get new url
            val uploadImageTask = uploadProfilePictureToStorage(currentUserUID, imageUri)
            val newProfilePictureUrl = getProfilePictureUrl(uploadImageTask)

            updateProfilePictureUrlOnPosts(newProfilePictureUrl)

            return UserInfoDb(currentUserUID, username, first, last, bio, newProfilePictureUrl)
        }
        return null
    }

    private fun getCurrentProfileInfo() {
        val currentUserUID = Firebase.auth.currentUser!!.uid
        val collectionPath = ("usersInfo")
        val infoDataRef = database.collection(collectionPath)

        CoroutineScope(Dispatchers.IO).launch {
            val document = infoDataRef
                .document(currentUserUID)
                .get()
                .await()

            val item = document.toObject<UserInfoDb>()
            if (item != null) {
                infoList.add(item)
                profilePictureUrl = item.profilePicture.toString()
            }
            withContext(Dispatchers.Main) {
                setInfoDataInViews(infoList)
            }
        }
    }

    private fun setInfoDataInViews(infoList: ArrayList<UserInfoDb>) {
        val ivEditProfilePicture = view?.findViewById<ImageView>(R.id.ivEditProfilePicture)

        tvEditUsername.text = infoList[0].username

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.default_profile_picture)
            .centerCrop()

        if (ivEditProfilePicture != null) {
            Glide.with(this)
                .applyDefaultRequestOptions(requestOptions)
                .load(infoList[0].profilePicture)
                .into(ivEditProfilePicture)
        }

        etFirstName.setText(infoList[0].first)
        etLastName.setText(infoList[0].last)
        etBio.setText(infoList[0].bio)
    }

    private suspend fun uploadProfilePictureToStorage(
        userUID: String,
        profilePictureUri: Uri
    ): UploadTask.TaskSnapshot? {
        val childPath = "profilePictures/$userUID"
        val imageRef = Firebase.storage.reference.child(childPath)

        ivEditProfilePicture.alpha = 0.5F
        pbEditImageUpload.visibility = View.VISIBLE

        val uploadProfilePictureTask = imageRef.putFile(profilePictureUri)
            .addOnProgressListener {
                pbEditImageUpload.max = it.totalByteCount.toInt()
                pbEditImageUpload.progress = it.bytesTransferred.toInt()
            }.addOnSuccessListener {
                pbEditImageUpload.visibility = View.INVISIBLE
                ivEditProfilePicture.alpha = 1F
            }.await()

        return uploadProfilePictureTask
    }

    private suspend fun getProfilePictureUrl(uploadImageTask: UploadTask.TaskSnapshot?): String {
        if (uploadImageTask != null && uploadImageTask.metadata != null && uploadImageTask.metadata!!.reference != null) {
            val url = uploadImageTask.storage.downloadUrl
                .await()
            val downloadUrl = url.toString()

            return downloadUrl
        }
        return ""
    }

    private suspend fun updateProfilePictureUrlOnPosts(profilePictureUrl: String) =
        CoroutineScope(Dispatchers.IO).launch {
            val dataRef = database.collection("posts")

            getUsersPostsList()

            for (item in 0 until usersPostList.size) {
                dataRef.document(usersPostList[item].documentUID)
                    .update("userProfilePicture", profilePictureUrl)
                    .await()
            }
        }

    private suspend fun getUsersPostsList() {
        val currentUserUID = Firebase.auth.uid

        val dataRef = database.collection("usersInfo/$currentUserUID/posts")

        dataRef.whereEqualTo("dummyBool", true).get().addOnSuccessListener { documents ->
            usersPostList.clear()

            for (document in documents) {
                val item = document.toObject<UsersPostDb>()
                usersPostList.add(item)
            }
        }.await()
    }

    private suspend fun uploadProfileDataToFirestore() =
        CoroutineScope(Dispatchers.Main).launch {
            val currentUserUID = Firebase.auth.currentUser!!.uid
            val collectionPath = ("usersInfo")
            val profileInfoDb = getEditedData()
            if (profileInfoDb != null) {
                database.collection(collectionPath).document(currentUserUID).set(profileInfoDb)
                    .await()

                Toast.makeText(context, "Profile Updated", Toast.LENGTH_LONG).show()
                activity?.onBackPressed()
            }
        }
}