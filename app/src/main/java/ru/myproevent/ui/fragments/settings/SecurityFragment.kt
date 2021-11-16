package ru.myproevent.ui.fragments.settings

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.KeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout
import moxy.ktx.moxyPresenter
import ru.myproevent.ProEventApp
import ru.myproevent.databinding.FragmentSecurityBinding
import ru.myproevent.domain.model.ProfileDto
import ru.myproevent.ui.BackButtonListener
import ru.myproevent.ui.fragments.BaseMvpFragment
import ru.myproevent.ui.presenters.settings.security.SecurityPresenter
import ru.myproevent.ui.presenters.settings.security.SecurityView
import ru.myproevent.ui.views.KeyboardAwareTextInputEditText

// TODO: рефакторинг: Данный фрагмент во многом копирует AccountFragment. Вынести общее в абстрактынй класс
class SecurityFragment : BaseMvpFragment(), SecurityView, BackButtonListener {
    private var _view: FragmentSecurityBinding? = null
    private val view get() = _view!!

    override val presenter by moxyPresenter {
        SecurityPresenter().apply {
            ProEventApp.instance.appComponent.inject(this)
        }
    }

    private fun showKeyBoard(view: View) {
        val imm: InputMethodManager =
            requireContext().getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setEditListeners(
        textInput: TextInputLayout,
        textEdit: KeyboardAwareTextInputEditText
    ) {
        textEdit.keyListener = null
        textInput.setEndIconOnClickListener {
            textEdit.keyListener = defaultKeyListener
            textEdit.requestFocus()
            showKeyBoard(textEdit)
            textEdit.text?.let { it1 -> textEdit.setSelection(it1.length) }
            textInput.endIconMode = TextInputLayout.END_ICON_NONE
            showSaveButton()
        }
    }

    private fun showSaveButton() {
        if (view.save.visibility == GONE) {
            view.save.visibility = VISIBLE
        }
    }

    private lateinit var defaultKeyListener: KeyListener

    companion object {
        fun newInstance() = SecurityFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _view = FragmentSecurityBinding.inflate(inflater, container, false).apply {
            defaultKeyListener = emailEdit.keyListener
            setEditListeners(emailInput, emailEdit)
            setEditListeners(loginInput, loginEdit)
            newPasswordEdit.doAfterTextChanged { showSaveButton() }
            save.setOnClickListener {
                if (oldPasswordEdit.text.toString() != newPasswordEdit.text.toString() || oldPasswordEdit.text.toString() != confirmPasswordEdit.text.toString()) {
                    Toast.makeText(context, "Пароли не совпадают", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                presenter.saveProfile(
                    emailEdit.text.toString(),
                    loginEdit.text.toString(),
                    newPasswordEdit.text.toString(),
                )
            }
            titleButton.setOnClickListener { presenter.backPressed() }
        }
        presenter.getProfile()
        return view.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _view = null
    }

    override fun showProfile(profileDto: ProfileDto) {
        with(view) {
            with(profileDto) {
                nickName?.let { loginEdit.text = SpannableStringBuilder(it) }
            }
        }
    }

    override fun makeProfileEditable() {
        // TODO:
        Toast.makeText(context, "makeProfileEditable()", Toast.LENGTH_LONG).show()
    }

    override fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}