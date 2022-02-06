package com.pandey.suno.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.pandey.suno.R
import com.pandey.suno.adapter.EpisodeListAdapter
import com.pandey.suno.databinding.FragmentPodcastDetailsBinding
import com.pandey.suno.viewmodel.PodcastViewModel

class PodcastDetailsFragment : Fragment() {

    private val podcastViewModel: PodcastViewModel by activityViewModels()
    private lateinit var databinding: FragmentPodcastDetailsBinding
    private lateinit var episodeListAdapter: EpisodeListAdapter

    companion object {
        fun newInstance(): PodcastDetailsFragment {
            return PodcastDetailsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        databinding = FragmentPodcastDetailsBinding.inflate(inflater, container, false)
        return databinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        podcastViewModel.podcastLiveData.observe(viewLifecycleOwner, Observer { viewData ->
            if (viewData != null) {
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
                    databinding.episodeRecyclerView.context, layoutManager.orientation)
                databinding.episodeRecyclerView.addItemDecoration(dividerItemDecoration)
                episodeListAdapter = EpisodeListAdapter(viewData.episodes)
                databinding.episodeRecyclerView.adapter = episodeListAdapter
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_details, menu)
    }
}