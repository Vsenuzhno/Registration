package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.activity.FeedFragment.Companion.urlArg
import ru.netology.nmedia.databinding.FragmentPhotoViewBinding
import ru.netology.nmedia.view.load


class PhotoView : Fragment() {

    private var fragmentBinding: FragmentPhotoViewBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPhotoViewBinding.inflate(
            inflater,
            container,
            false
        )
        fragmentBinding = binding
        val photo = arguments?.urlArg
        binding.attachment.load("${BuildConfig.BASE_URL}/media/${photo}")
        return binding.root
    }
}
