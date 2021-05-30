package com.petophion.presensect.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.petophion.presensect.R
import com.petophion.presensect.activities.main.bottomnavbar.feed.FeedFragment
import com.petophion.presensect.databases.FeedPostDb
import kotlinx.android.synthetic.main.cv_post_preview_image_based.view.*

class FeedRecyclerViewAdapter(
    private val listener: FeedFragment,
) : RecyclerView.Adapter<FeedRecyclerViewAdapter.FeedViewHolder>() {

    private var postItems: ArrayList<FeedPostDb> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.cv_post_preview_image_based, parent, false)
        return FeedViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val currentPostItem = postItems[position]
        holder.bind(currentPostItem)
    }

    override fun getItemCount() = postItems.size

    fun submitList(feedPostList: ArrayList<FeedPostDb>) {
        val oldFeedPostList = postItems
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            FeedPostDiffItemCallback(
                oldFeedPostList,
                feedPostList
            )
        )

        postItems = feedPostList
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged() //optimize in future
    }

    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private val tvUsername: TextView = itemView.tvPostUsername
        private val ivProfilePicture: ImageView = itemView.ivPostProfilePicture
        private val ivImage: ImageView = itemView.ivPostImage
        private val tvTitle: TextView = itemView.tvPostTitle
        private val tvTimestamp: TextView = itemView.tvPostTimestamp

        fun bind(feedPostDb: FeedPostDb) {
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_error)
                .centerCrop()

            Glide.with(itemView.context)
                .applyDefaultRequestOptions(requestOptions)
                .load(feedPostDb.imageUrl)
                .into(ivImage)

            Glide.with(itemView.context)
                .applyDefaultRequestOptions(requestOptions)
                .load(feedPostDb.userProfilePicture)
                .into(ivProfilePicture)

            tvUsername.text = feedPostDb.username
            tvTitle.text = feedPostDb.title
            tvTimestamp.text = getTimeDifferenceFromCurrentTime(feedPostDb.timestamp)
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    class FeedPostDiffItemCallback(
        private var oldFeedPostList: ArrayList<FeedPostDb>,
        private var newFeedPostList: ArrayList<FeedPostDb>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldFeedPostList.size
        }

        override fun getNewListSize(): Int {
            return newFeedPostList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return (oldFeedPostList[oldItemPosition].postUID == (newFeedPostList[newItemPosition].postUID))
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldFeedPostList[oldItemPosition] == newFeedPostList[newItemPosition]
        }
    }


    fun getTimeDifferenceFromCurrentTime(postTimestamp: Timestamp?): CharSequence? {

        val currentTimestamp = Timestamp.now()

        val postTime = postTimestamp?.toDate()?.time
        val currentTime = currentTimestamp.toDate().time

        val timeDiff = postTime?.let { DateUtils.getRelativeTimeSpanString(it, currentTime, 0) }

        return timeDiff

    }

}