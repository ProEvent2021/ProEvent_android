package ru.myproevent.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.androidx.AppNavigator
import ru.myproevent.ProEventApp
import ru.myproevent.R
import ru.myproevent.databinding.FragmentSettingsMainBinding
import ru.myproevent.ui.BackButtonListener
import ru.myproevent.ui.fragments.BaseMvpFragment
import ru.myproevent.ui.presenters.main.MainView
import ru.myproevent.ui.presenters.main.Menu
import ru.myproevent.ui.presenters.settings.main.SettingsMainPresenter
import ru.myproevent.ui.presenters.settings.main.SettingsMainView
import javax.inject.Inject
import javax.inject.Named
import moxy.presenter.ProvidePresenter
import moxy.presenter.InjectPresenter

class SettingsMainFragment : BaseMvpFragment(), SettingsMainView, BackButtonListener {

    private val navigator: Navigator by lazy {
        object : AppNavigator(requireActivity(), R.id.settings_container, childFragmentManager) {
            override fun setupFragmentTransaction(
                fragmentTransaction: FragmentTransaction,
                currentFragment: Fragment?,
                nextFragment: Fragment?
            ) {
                setVerticalTransitionAnimation(
                    currentFragment,
                    nextFragment,
                    fragmentTransaction
                )
            }
        }
    }

    private fun setVerticalTransitionAnimation(
        currFragment: Fragment?,
        nextFragment: Fragment?,
        fragmentTransaction: FragmentTransaction
    ) {
        fragmentTransaction.setCustomAnimations(
            R.anim.enter_from_bottom,
            R.anim.exit_to_bottom,
            R.anim.enter_from_bottom,
            R.anim.exit_to_bottom
        )
    }

    @Inject
    @Named("SettingsNavigatorHolder")
    lateinit var navigatorHolder: NavigatorHolder

    companion object {
        fun newInstance() = SettingsMainFragment()
    }

//    override val presenter by moxyPresenter {
//        SettingsMainPresenter().apply {
//            ProEventApp.instance.appComponent.inject(this)
//        }
//    }

    @Inject
    @InjectPresenter
    override lateinit var presenter: SettingsMainPresenter

    @ProvidePresenter
    fun providePresenter(): SettingsMainPresenter {
        return presenter
    }

    private var _view: FragmentSettingsMainBinding? = null
    private val view get() = _view!!

    override fun onCreate(savedInstanceState: Bundle?) {
        ProEventApp.instance.appComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as MainView).selectItem(Menu.SETTINGS)
        _view = FragmentSettingsMainBinding.inflate(inflater, container, false)
        return view.root
    }

    //override fun onResumeFragments() {
    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        _view = null
    }
}