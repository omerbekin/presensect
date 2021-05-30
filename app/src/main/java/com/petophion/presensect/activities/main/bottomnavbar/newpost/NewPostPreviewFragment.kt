package com.petophion.presensect.activities.main.bottomnavbar.newpost

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.petophion.presensect.R
import com.petophion.presensect.datasources.CreateNewPost
import com.petophion.presensect.datasources.FeedSource.Companion.getProfileUsername
import kotlinx.android.synthetic.main.fragment_new_post.ivPostImage
import kotlinx.android.synthetic.main.fragment_new_post_preview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewPostPreviewFragment : Fragment() {

    lateinit var imageUri: Uri
    lateinit var title: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_new_post_preview, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val bundle = this.arguments
        val image = bundle?.getString("image")
        val title = bundle?.getString("title")

        imageUri = image?.toUri()!!

        ivPostImage.setImageURI(imageUri)
        tvPostTitle.text = title

        CoroutineScope(Dispatchers.Main).launch {
            val currentUserUID = Firebase.auth.uid
            val username = currentUserUID?.let { getProfileUsername(it) }
            tvPostUsername.text = username
        }

        btn_share_post.setOnClickListener {
            val createNewPost = CreateNewPost()

            if (title != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    createNewPost.uploadPost(imageUri, title)
                }
            }

            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.popBackStack()
            fragmentManager.popBackStack()

        }
    }
}