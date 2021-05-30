package com.petophion.presensect.activities.login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.petophion.presensect.R
import com.petophion.presensect.databases.UserInfoDb
import kotlinx.android.synthetic.main.fragment_add_profile_picture.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddProfilePictureFragment : Fragment() {

    private lateinit var imageUri: Uri

    private val DEFAULT_PROFILE_PICTURE_URL =
        "https://firebasestorage.googleapis.com/v0/b/presensect.appspot.com/o/profilePictures%2Fdefault_profile_picture.png?alt=media&token=222730a2-550f-46b0-930f-61ff56c35840"

    private var isImagePicked = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val bundle = this.arguments
        val newUserUID = bundle?.getString("newUserUID", "error")
        val username = bundle?.getString("username")

        btnAddProfilePicture.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 0)
        }

        btnContinueToAdditionalInfo.setOnClickListener {

            val additionalInfoFragment = AdditionalInfoFragment()
            val bundleForNext = Bundle()

            additionalInfoFragment.arguments = bundle

            bundleForNext.putString("newUserUID", newUserUID)
            bundleForNext.putString("username", username)


            CoroutineScope(Dispatchers.IO).launch {
                if (newUserUID != null) {
                    if (isImagePicked) {

                        pbAddProfilePicture.visibility = View.VISIBLE

                        val uploadImageTask = uploadProfilePictureToStorage(newUserUID, imageUri)
                        val profilePictureUrl = getImageUrl(uploadImageTask)

                        val profileInfoDb =
                            UserInfoDb(
                                newUserUID,
                                username,
                                "",
                                "",
                                "",
                                profilePictureUrl
                            )

                        uploadUserInfo(newUserUID, profileInfoDb)

                        bundleForNext.putString("imageUrl", profilePictureUrl)
                    } else if (!isImagePicked) {

                        val profileInfoDb =
                            UserInfoDb(
                                newUserUID,
                                username,
                                "",
                                "",
                                "",
                                DEFAULT_PROFILE_PICTURE_URL
                            )

                        uploadUserInfo(newUserUID, profileInfoDb)

                        bundleForNext.putString("imageUrl", DEFAULT_PROFILE_PICTURE_URL)
                    }
                }

                val transaction = childFragmentManager.beginTransaction()
                transaction.replace(
                    R.id.clProfilePictureContainer,
                    additionalInfoFragment,
                    "profilePicture"
                )
                transaction.commit()
                transaction.addToBackStack("profilePicture")

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_profile_picture, container, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 0 && data != null) {
            imageUri = data.data!!

            ivAddProfilePicture.setImageURI(imageUri)

            btnAddProfilePicture.text = "Change Image"
            btnContinueToAdditionalInfo.text = "Continue"

            isImagePicked = true
        }
    }

    private suspend fun uploadProfilePictureToStorage(
        userUID: String,
        imageUri: Uri
    ): UploadTask.TaskSnapshot? {
        val childPath = "profilePictures/$userUID"
        val imageRef = Firebase.storage.reference.child(childPath)

        ivAddProfilePicture.alpha = 0.5F

        val uploadProfilePictureTask = imageRef.putFile(imageUri)
            .addOnProgressListener {
                pbAddProfilePicture.max = it.totalByteCount.toInt()
                pbAddProfilePicture.progress = it.bytesTransferred.toInt()
            }.addOnSuccessListener {
                pbAddProfilePicture.visibility = View.GONE
                ivAddProfilePicture.alpha = 1F
            }.await()


        return uploadProfilePictureTask
    }

    private suspend fun getImageUrl(uploadImageTask: UploadTask.TaskSnapshot?): String {
        if (uploadImageTask != null && uploadImageTask.metadata != null && uploadImageTask.metadata!!.reference != null) {
            val url = uploadImageTask.storage.downloadUrl
                .await()
            val downloadUrl = url.toString()

            return downloadUrl
        }
        return ""
    }

    private suspend fun uploadUserInfo(newUserUID: String, userInfoDb: UserInfoDb) {

        val database = Firebase.firestore
        val infoDataRef = database.collection("usersInfo")

        infoDataRef.document(newUserUID).set(userInfoDb).await()
    }
}