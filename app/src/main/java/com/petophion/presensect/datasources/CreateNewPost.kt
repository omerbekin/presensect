package com.petophion.presensect.datasources

import android.net.Uri
import com.google.firebase.Timestamp.now
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.petophion.presensect.databases.FeedPostDb
import com.petophion.presensect.datasources.FeedSource.Companion.getProfilePicture
import com.petophion.presensect.datasources.FeedSource.Companion.getProfileUsername
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class CreateNewPost {

    private val storageRef: StorageReference = Firebase.storage.reference
    private val database = Firebase.firestore
    private lateinit var feedPostDb: FeedPostDb


    fun uploadPost(imageUri: Uri, title: String) = CoroutineScope(Dispatchers.IO).launch {
        val postUID = UUID.randomUUID().toString()

        val uploadImageTask = uploadImageToStorage(postUID, imageUri)
        val downloadUrl = getImageUrl(uploadImageTask)
        collectPostData(postUID, downloadUrl, title)
    }

    private suspend fun uploadImageToStorage(postUID: String, imageUri: Uri): UploadTask.TaskSnapshot? {
        val currentUserUID = Firebase.auth.currentUser!!.uid
        val postType = "images"
        val childPath = "posts/$currentUserUID/$postType/$postUID"
        val imageRef = storageRef.child(childPath)

        val uploadImageTask = imageRef.putFile(imageUri)
            .await()

        return uploadImageTask
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

    private fun collectPostData(postUID: String, imageUrl: String, title: String) =
        CoroutineScope(Dispatchers.IO).launch {
            val currentUserUID = Firebase.auth.currentUser!!.uid
            val username = getProfileUsername(currentUserUID)
            val userProfilePicture = getProfilePicture(currentUserUID)
            val timestamp = now()
            val visible = true

            feedPostDb =
                FeedPostDb(postUID, currentUserUID, username!!, title, imageUrl, userProfilePicture!!, timestamp, visible)

            uploadPostDataToFirestore(postUID)
        }

    private suspend fun uploadPostDataToFirestore(postUID: String) =
        CoroutineScope(Dispatchers.IO).launch {
            database.collection("posts").document(postUID).set(feedPostDb)
                .await()

            setPostUidToUsersCollection(postUID)
        }

    private suspend fun setPostUidToUsersCollection (postUID: String) = CoroutineScope(Dispatchers.IO).launch {
        val currentUserUID = Firebase.auth.currentUser!!.uid
        val documentUID = hashMapOf(
            "documentUID" to postUID
        )

        database.collection("usersInfo/$currentUserUID/posts")
            .document(postUID)
            .set(documentUID)
            .await()
    }
}
