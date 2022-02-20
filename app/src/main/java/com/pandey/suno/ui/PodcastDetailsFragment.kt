package com.pandey.suno.ui

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.pandey.suno.R
import com.pandey.suno.adapter.EpisodeListAdapter
import com.pandey.suno.databinding.FragmentPodcastDetailsBinding
import com.pandey.suno.service.SunoMediaService
import com.pandey.suno.viewmodel.PodcastViewModel

class PodcastDetailsFragment : Fragment(), EpisodeListAdapter.EpisodeListAdapterListener {

    private val podcastViewModel: PodcastViewModel by activityViewModels()
    private lateinit var databinding: FragmentPodcastDetailsBinding
    private lateinit var episodeListAdapter: EpisodeListAdapter
    private var listener: OnPodcastDetailsListener? = null


    companion object {
        fun newInstance(): PodcastDetailsFragment {
            return PodcastDetailsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        databinding = FragmentPodcastDetailsBinding.inflate(inflater, container, false)
        return databinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        podcastViewModel.podcastLiveData.observe(viewLifecycleOwner, Observer { viewData ->
            if (viewData != null) {
                activity?.invalidateOptionsMenu()

                databinding.feedTitleTextView.text = viewData.feedTitle
                databinding.feedDescTextView.text = viewData.feedDesc
                activity?.let { activity ->
                    Glide.with(activity).load(viewData.imageUrl).into(databinding.feedImageView)
                }


                databinding.feedDescTextView.movementMethod = ScrollingMovementMethod()
                databinding.episodeRecyclerView.setHasFixedSize(true)

                val layoutManager = LinearLayoutManager(activity)
                databinding.episodeRecyclerView.layoutManager = layoutManager

                val dividerItemDecoration = DividerItemDecoration(
                    databinding.episodeRecyclerView.context, layoutManager.orientation
                )
                databinding.episodeRecyclerView.addItemDecoration(dividerItemDecoration)
                episodeListAdapter = EpisodeListAdapter(viewData.episodes, this)
                databinding.episodeRecyclerView.adapter = episodeListAdapter
            }
        })

        subscriptionToggle()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPodcastDetailsListener) {
            listener = context
        } else {
            throw RuntimeException(
                context.toString() +
                        " must implement OnPodcastDetailsListener"
            )
        }
    }




   

    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()

    }

    private fun updateControls() {
        val viewData = podcastViewModel.podcastLiveData
        databinding.feedTitleTextView.text = viewData.value?.feedTitle
        databinding.feedDescTextView.text = viewData.value?.feedDesc
        activity?.let { activity ->
            Glide.with(activity).load(viewData.value?.imageUrl).into(databinding.feedImageView)
        }
    }


    interface OnPodcastDetailsListener {
        fun onSubscribe()
        fun onUnsubscribe()
        fun onShowEpisodePlayer(episodeViewData: PodcastViewModel.EpisodeViewData)
    }

    override fun onSelectedEpisode(episodeViewData: PodcastViewModel.EpisodeViewData) {
        /* val fragmentActivity = activity as FragmentActivity
         val controller = MediaControllerCompat.getMediaController(fragmentActivity)
         if (controller.playbackState != null) {
             if (controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                 controller.transportControls.pause()
             } else {
                 startPlaying(episodeViewData)
             }
         } else {
             startPlaying(episodeViewData)
         }
         */
        listener?.onShowEpisodePlayer(episodeViewData)


    }


    private fun subscriptionToggle(){
        podcastViewModel.podcastLiveData.observe(viewLifecycleOwner, Observer { podcast ->
            if (podcast != null) {

                val textValue = if (podcast.subscribed) {
                    getString(R.string.unsubscribe)
                } else {
                    getString(R.string.subscribe)

                }
                databinding.subscribeButton.text = textValue
            }
        })

        databinding.subscribeButton.setOnClickListener {

            if (databinding.subscribeButton.text == getString(R.string.unsubscribe)) {
                listener?.onUnsubscribe()
            } else {
                listener?.onSubscribe()
            }
        }



    }



}