package ru.myproevent.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moxy.ktx.moxyPresenter
import ru.myproevent.ProEventApp
import ru.myproevent.databinding.FragmentSettingsListBinding
import ru.myproevent.ui.BackButtonListener
import ru.myproevent.ui.fragments.BaseMvpFragment
import ru.myproevent.ui.presenters.main.MainView
import ru.myproevent.ui.presenters.main.Menu
import ru.myproevent.ui.presenters.settings.list.SettingsListPresenter
import ru.myproevent.ui.presenters.settings.list.SettingsListView

class SettingsListListFragment : BaseMvpFragment(), SettingsListView, BackButtonListener {
    private var _view: FragmentSettingsListBinding? = null
    private val view get() = _view!!

    override val presenter by moxyPresenter {
        SettingsListPresenter().apply {
            ProEventApp.instance.appComponent.inject(this)
        }
    }

    companion object {
        fun newInstance() = SettingsListListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as MainView).selectItem(Menu.SETTINGS)
        _view = FragmentSettingsListBinding.inflate(inflater, container, false).apply {
            account.setOnClickListener { presenter.account() }
            security.setOnClickListener { presenter.security() }
            subscriptions.setOnClickListener { }
            help.setOnClickListener { }
            about.setOnClickListener { }
            logout.setOnClickListener { presenter.logout() }
        }
        return view.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _view = null
    }
}