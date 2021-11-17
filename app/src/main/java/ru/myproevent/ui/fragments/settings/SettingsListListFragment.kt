package ru.myproevent.ui.fragments.settings

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.terrakok.cicerone.Router
import moxy.ktx.moxyPresenter
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import ru.myproevent.ProEventApp
import ru.myproevent.databinding.FragmentSettingsListBinding
import ru.myproevent.ui.BackButtonListener
import ru.myproevent.ui.fragments.BaseMvpFragment
import ru.myproevent.ui.presenters.main.MainView
import ru.myproevent.ui.presenters.main.Menu
import ru.myproevent.ui.presenters.settings.list.SettingsListPresenter
import ru.myproevent.ui.presenters.settings.list.SettingsListView
import ru.myproevent.ui.presenters.settings.main.SettingsMainPresenter
import ru.myproevent.ui.screens.IScreens
import javax.inject.Inject
import javax.inject.Named

class SettingsListListFragment : BaseMvpFragment(), SettingsListView, BackButtonListener {
    private var _view: FragmentSettingsListBinding? = null
    private val view get() = _view!!

//    override val presenter by moxyPresenter {
//        SettingsListPresenter().apply {
//            ProEventApp.instance.appComponent.inject(this)
//        }
//    }

    @Inject
    @InjectPresenter
    override lateinit var presenter: SettingsListPresenter

    @ProvidePresenter
    fun providePresenter(): SettingsListPresenter {
        return presenter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        ProEventApp.instance.appComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    @Inject
    @Named("SettingsRouter")
    lateinit var localRouter: Router

    @Inject
    lateinit var screens: IScreens

    override fun showAccount() {
        Log.d("[MYLOG]", "localRouter.navigateTo(screens.account())")
        localRouter.navigateTo(screens.account())
    }

    override fun showSecurity() {
        localRouter.navigateTo(screens.security())
    }
}