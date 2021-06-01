package com.petophion.presensect.activities.main.bottomnavbar.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.petophion.presensect.TopSpacingItemDecoration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.petophion.presensect.R
import com.petophion.presensect.adapters.FeedRecyclerViewAdapter
import com.petophion.presensect.datasources.FeedSource
import com.petophion.presensect.datasources.FeedSource.Companion.createDataSet
import com.petophion.presensect.datasources.FeedSource.Companion.postList
import kotlinx.android.synthetic.main.cv_post_preview_image_based.*
import kotlinx.android.synthetic.main.fragment_feed.*

class FeedFragment : Fragment(), FeedRecyclerViewAdapter.OnItemClickListener {

    private var feedAdapter = FeedRecyclerViewAdapter(this)
    private val database = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feed, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        swipeRefreshLayout.isRefreshing = true

        initRecyclerView()
        addDataSet()

        swipeRefreshLayout.isRefreshing = false

        swipeRefreshLayout.setOnRefreshListener {
            refreshDataFromFirestore(feedAdapter)
            feedAdapter.notifyDataSetChanged()

            swipeRefreshLayout.isRefreshing = false
        }

        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    //RecyclerView item onClick event
    override fun onItemClick(position: Int) {
//        val clickedItem = FeedSource.createDataSet()[position]
    }

    private fun initRecyclerView() {
        rvFeed.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecorator = TopSpacingItemDecoration(15)
            addItemDecoration(topSpacingDecorator)
            adapter = feedAdapter
            setHasFixedSize(true)
        }
    }

    private fun addDataSet() {
        createDataSet(feedAdapter)
    }

    private fun refreshDataFromFirestore(feedAdapter: FeedRecyclerViewAdapter) {
        swipeRefreshLayout.isRefreshing = true

        val postDataRef = database.collection("posts").whereEqualTo("visible", true)

        postDataRef.get().addOnSuccessListener { documents ->
            postList.clear()
            FeedSource.setPostDataToPostList(documents, feedAdapter)
            swipeRefreshLayout.isRefreshing = false
        }
    }
}