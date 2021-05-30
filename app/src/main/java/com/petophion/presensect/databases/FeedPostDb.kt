package com.petophion.presensect.databases

import com.google.firebase.Timestamp

data class FeedPostDb(
    val postUID: String = "",
    val userUID: String = "",
    val username: String = "",
    val title: String = "",
    var imageUrl: String = "",
    var userProfilePicture: String = "",
    val timestamp: Timestamp? = null,
    val visible: Boolean = true
) {
    override fun equals(other: Any?): Boolean {

        if (javaClass != other?.javaClass) {
            return false
        }

        other as FeedPostDb

        if (postUID != other.postUID) {
            return false
        }
        if (userUID != other.userUID) {
            return false
        }
        if (username != other.username) {
            return false
        }
        if (title != other.title) {
            return false
        }
        if (imageUrl != other.imageUrl) {
            return false
        }
        if (userProfilePicture != other.userProfilePicture) {
            return false
        }
        if (timestamp != other.timestamp) {
            return false
        }
        if (visible != other.visible) {
            return false
        }

        return true
    }
}