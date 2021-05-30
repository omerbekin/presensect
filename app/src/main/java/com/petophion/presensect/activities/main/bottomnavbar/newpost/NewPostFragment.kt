package com.petophion.presensect.activities.main.bottomnavbar.newpost

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.petophion.presensect.R
import kotlinx.android.synthetic.main.fragment_new_post.*
import kotlinx.android.synthetic.main.fragment_new_post.btnChooseImage
import kotlinx.android.synthetic.main.fragment_new_post.etPostTitle

class NewPostFragment : Fragment() {

    private lateinit var root: View

    private val bundle = Bundle()

    private lateinit var imageUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        root = inflater.inflate(R.layout.fragment_new_post, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnChooseImage.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 0)
        }

        btnNextFragment.visibility = View.GONE

        btnNextFragment.setOnClickListener {

            val image = imageUri.toString()
            val title = etPostTitle.text.toString()

            if (image == "" && title == "") {
                Toast.makeText(context, "Please pick an image and a title to continue", Toast.LENGTH_SHORT).show()
            } else if (image == "") {
                Toast.makeText(context, "Please pick an image to continue", Toast.LENGTH_SHORT).show()
            } else if (title == "") {
                Toast.makeText(context, "Please pick a title to continue", Toast.LENGTH_SHORT).show()
            } else {

                val newPostPreviewFragment = NewPostPreviewFragment()
                newPostPreviewFragment.arguments = bundle

                bundle.putString("image", image)
                bundle.putString("title", title)

                val transaction = childFragmentManager.beginTransaction()
                transaction.replace(R.id.clNewPostContainer, newPostPreviewFragment, "New Post")
                transaction.commit()
                transaction.addToBackStack("New Post")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 0 && data != null) {
            imageUri = data.data!!
            ivPostImage.setImageURI(imageUri)
            btnChooseImage.text = "Change Image"
            btnNextFragment.visibility = View.VISIBLE
        }
    }
}