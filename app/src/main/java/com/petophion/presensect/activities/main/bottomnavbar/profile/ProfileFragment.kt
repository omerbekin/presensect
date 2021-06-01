package com.petophion.presensect.activities.main.bottomnavbar.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.petophion.presensect.R
import com.petophion.presensect.activities.login.LoginActivity
import com.petophion.presensect.databases.UserInfoDb
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.*

class ProfileFragment : Fragment() {

    var infoList = ArrayList<UserInfoDb>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        ivProfilePicture.clipToOutline = true

        setViewVisibilities()
        getCurrentProfileInfo()

        btnEditProfile.setOnClickListener {
            val profileEditFragment = EditProfileFragment()


            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.cl_profile_container, profileEditFragment, "Profile Edit")
            transaction.commit()
            transaction.addToBackStack("Profile Edit")
        }

        btnLogout.setOnClickListener{

            confirmLogout()

        }
    }

    private var instance: ProfileFragment? = null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        getCurrentProfileInfo()
    }

    private fun setViewVisibilities() {
        View.GONE.apply {
            tvUsername.visibility
            tvFirstName.visibility
            tvLastName.visibility
            tvBio.visibility
        }
    }

    private fun getCurrentProfileInfo() {
        val currentUserUID = Firebase.auth.currentUser!!.uid
        val database = Firebase.firestore
        val collectionPath = ("usersInfo")
        val infoDataRef = database.collection(collectionPath)

        CoroutineScope(Dispatchers.Main).launch {
            infoDataRef.document(currentUserUID)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        return@addSnapshotListener
                    }
                    querySnapshot?.let { document ->
                        val item = document.toObject<UserInfoDb>()
                        if (item != null) {
                            infoList.clear()
                            infoList.add(item)
                        }
                        //call activity scope
                        GlobalScope.launch {
                            withContext(Dispatchers.Main) {
                                setInfoDataInViews(infoList)
                            }
                        }
                    }
                }
        }
    }

    private fun setInfoDataInViews(infoList: ArrayList<UserInfoDb>) {

        val ivProfilePicture = view?.findViewById<ImageView>(R.id.ivProfilePicture)

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.default_profile_picture)
            .centerCrop()

        if (ivProfilePicture != null) {
            Glide.with(this)
                .applyDefaultRequestOptions(requestOptions)
                .load(infoList[0].profilePicture)
                .into(ivProfilePicture)
        }

        tvUsername.text = infoList[0].username
        tvFirstName.text = infoList[0].first
        tvLastName.text = infoList[0].last
        tvBio.text = infoList[0].bio


        View.VISIBLE.apply {
            tvUsername.visibility
            tvFirstName.visibility
            tvLastName.visibility
            tvBio.visibility
        }
    }

    private fun confirmLogout() {
        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.setMessage("Are you sure you want to logout?")
            ?.setCancelable(true)
            ?.setPositiveButton("Yes") { dialog, id ->
                Firebase.auth.signOut()
                reLogIn()
            }
            ?.setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder?.create()
        alert?.show()
    }

    private fun reLogIn() {
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}