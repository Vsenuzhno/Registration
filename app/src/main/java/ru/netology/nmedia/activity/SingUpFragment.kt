package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSingUpBinding
import ru.netology.nmedia.dto.NewUser
import ru.netology.nmedia.viewmodel.AuthViewModel


class SingUpFragment : Fragment() {
    private val viewModel: AuthViewModel by activityViewModels()
    private var _binding: FragmentSingUpBinding? = null
    private val mBinding get() = _binding!!
    private var newUser = NewUser("", "", "")


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingUpBinding.inflate(layoutInflater, container, false)

        mBinding.singUpButton.setOnClickListener {

            newUser = NewUser(
                mBinding.newuserlogin.text.toString(),
                mBinding.newusername.text.toString(),
                mBinding.newpassword.text.toString(),
            )

            if (mBinding.newpassword.text.toString() == mBinding.newpasswordverification.text.toString()) {
                if (newUser.login.isNotEmpty()
                    && newUser.password.isNotEmpty()
                    && newUser.nameNewUser.isNotEmpty()
                ) {
                    viewModel.singUp(newUser)
                    findNavController().navigateUp()
                } else Toast.makeText(context, getString(R.string.Enter_name), Toast.LENGTH_LONG)
                    .show()
            } else Toast.makeText(
                context,
                getString(R.string.passwords_dont_match),
                Toast.LENGTH_LONG
            )
                .show()
        }

        viewModel.dataState.observe(viewLifecycleOwner) {
            when (it) {
                0 -> findNavController().navigateUp()
                -1 -> {
                }

                1 -> {
                    Toast.makeText(context, R.string.Error, Toast.LENGTH_LONG)
                        .show()
                }

                else -> {
                    Toast.makeText(context, R.string.Error, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

        return mBinding.root
    }

}