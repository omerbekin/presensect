package com.petophion.presensect.datasources

import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.petophion.presensect.activities.main.bottomnavbar.feed.FeedFragment
import com.petophion.presensect.adapters.FeedRecyclerViewAdapter
import com.petophion.presensect.databases.FeedPostDb
import com.petophion.presensect.databases.UserInfoDb
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.collections.ArrayList

class FeedSource {


    companion object {

        var postList = ArrayList<FeedPostDb>()

        fun getDataFromFirestore(feedAdapter: FeedRecyclerViewAdapter) {
            val database = Firebase.firestore
            val postDataRef = database.collection("posts").whereEqualTo("visible", true)

            postDataRef
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException.let {
                        return@let
                    }
                    querySnapshot?.let { documents ->
                        setPostDataToPostList(documents, feedAdapter)
                    }
                }
        }

        fun createDataSet(feedAdapter: FeedRecyclerViewAdapter) {
            getDataFromFirestore(feedAdapter)
        }

        fun setPostDataToPostList(
            postQueryDocuments: QuerySnapshot,
            feedAdapter: FeedRecyclerViewAdapter
        ) {
            postList.clear()

            for (document in postQueryDocuments) {
                val item = document.toObject<FeedPostDb>()
                postList.add(item)
            }
            postList.sortByDescending { it.timestamp }
            feedAdapter.submitList(postList)
        }

        suspend fun getProfileUsername(userUID: String): String? {
            val database = Firebase.firestore
            val infoDataRef = database.collection("usersInfo")

            val document = infoDataRef
                .document(userUID)
                .get()
                .await()

            val item = document.toObject<UserInfoDb>()

            return item?.username
        }

        suspend fun getProfilePicture(userUID: String): String? {
            val database = Firebase.firestore
            val infoDataRef = database.collection("usersInfo")

            val document = infoDataRef
                .document(userUID)
                .get()
                .await()

            val item = document.toObject<UserInfoDb>()

            return item?.profilePicture
        }
    }
}