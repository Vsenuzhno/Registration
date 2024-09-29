package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()


    companion object {
        var Bundle.urlArg: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                if (authViewModel.isAuthorised) {
                    viewModel.likeById(post)
                } else {
                    AlertDialog.Builder(context)
                        .setMessage(R.string.only_for_registered_users)
                        .setPositiveButton(R.string.singUp) { _, _ ->
                            findNavController().navigate(
                                R.id.action_feedFragment_to_singUpFragment
                            )
                        }
                        .setNeutralButton(R.string.singIn) { _, _ ->
                            findNavController().navigate(
                                R.id.action_feedFragment_to_loginFragment
                            )
                        }
                        .setNegativeButton(R.string.cancel) { dialog, _ ->
                            dialog.cancel()
                        }
                        .setCancelable(true)
                        .create()
                        .show()
                }
            }

            override fun photoView(post: Post) {
                findNavController().navigate(R.id.action_feedFragment_to_photoView,
                    Bundle().apply { urlArg = post.attachment?.url })
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

        })

        var currentMenuProvider: MenuProvider? = null
        authViewModel.authLiveData.observe(viewLifecycleOwner) { authModel ->

            currentMenuProvider?.let(requireActivity()::removeMenuProvider)
            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.auth_menu, menu)
                    menu.setGroupVisible(R.id.authorized, authViewModel.isAuthorised)
                    menu.setGroupVisible(R.id.unAuthorized, !authViewModel.isAuthorised)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.singIn -> {
                            findNavController().navigate(R.id.action_feedFragment_to_loginFragment)
                            true
                        }

                        R.id.singUp -> {
                            findNavController().navigate(R.id.action_feedFragment_to_singUpFragment)
                            true
                        }

                        R.id.singOut -> {
                            AlertDialog.Builder(context)
                                .setTitle(R.string.are_you_suare)
                                .setPositiveButton(R.string.yes) { _, _ ->
                                    AppAuth.getInstance().removeUser()
                                }
                                .setNegativeButton(R.string.cancel) { dialog, _ ->
                                    dialog.cancel()
                                }
                                .setCancelable(true)
                                .create()
                                .show()
                            true
                        }

                        else -> false
                    }
                }

            }.also { currentMenuProvider = it }, viewLifecycleOwner)
        }

        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { data ->
            adapter.submitList(data.posts
                .filter { post -> !post.hidden }
            )
            binding.emptyText.isVisible = data.empty
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->

            if (state == 1) binding.newPostButton.visibility = View.VISIBLE

            println("Newer: $state")
        }

        binding.newPostButton.setOnClickListener {
            it.visibility = View.GONE
            viewModel.viewNewPost()
            binding.list.smoothScrollToPosition(0)
        }

        binding.swiperefresh.setOnRefreshListener {
            binding.newPostButton.visibility = View.GONE
            viewModel.refreshPosts()
        }

        binding.fab.setOnClickListener {
            if (authViewModel.isAuthorised) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                AlertDialog.Builder(context)
                    .setMessage(R.string.only_for_registered_users)
                    .setPositiveButton(R.string.singUp) { _, _ ->
                        findNavController().navigate(
                            R.id.action_feedFragment_to_singUpFragment
                        )
                    }
                    .setNeutralButton(R.string.singIn) { _, _ ->
                        findNavController().navigate(
                            R.id.action_feedFragment_to_loginFragment
                        )
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    .setCancelable(true)
                    .create()
                    .show()
            }
        }

        return binding.root
    }
}


