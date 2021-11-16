package ru.myproevent.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.androidx.AppNavigator
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter
import ru.myproevent.ProEventApp
import ru.myproevent.R
import ru.myproevent.databinding.ActivityMainBinding
import ru.myproevent.ui.BackButtonListener
import ru.myproevent.ui.presenters.main.MainPresenter
import ru.myproevent.ui.presenters.main.MainView
import ru.myproevent.ui.presenters.main.Menu
import javax.inject.Inject
import javax.inject.Named

class MainActivity : MvpAppCompatActivity(), MainView {

    private val navigator: Navigator = object : AppNavigator(this, R.id.container) {
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
    @Named("MainNavigatorHolder")
    lateinit var navigatorHolder: NavigatorHolder

    private val presenter by moxyPresenter {
        MainPresenter().apply {
            ProEventApp.instance.appComponent.inject(this)
        }
    }

    private var _view: ActivityMainBinding? = null
    private val view get() = _view!!

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Proevent_NoActionBar)
        super.onCreate(savedInstanceState)
        _view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)
        ProEventApp.instance.appComponent.inject(this)
        initBottomNavigationView()
    }

    private fun initBottomNavigationView() = with(view.bottomNavigationView) {
        setOnItemSelectedListener { item ->
            presenter.openScreen(
                when (item.itemId) {
                    R.id.home -> Menu.HOME
                    R.id.contacts -> Menu.CONTACTS
                    R.id.chat -> Menu.CHAT
                    R.id.events -> Menu.EVENTS
                    R.id.settings -> Menu.SETTINGS
                    else -> return@setOnItemSelectedListener false
                }
            )
            true
        }

        setOnItemReselectedListener {}
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun hideBottomNavigation() {
        if (view.bottomNavigationView.visibility == GONE) {
            return
        }
        view.bottomNavigationView.visibility = GONE
    }

    override fun showBottomNavigation() {
        if (view.bottomNavigationView.visibility == VISIBLE) {
            return
        }
        view.bottomNavigationView.visibility = VISIBLE
    }

    override fun selectItem(menu: Menu) {
        view.bottomNavigationView.menu.findItem(when(menu){
            Menu.HOME ->R.id.home
            Menu.CONTACTS ->R.id.contacts
            Menu.CHAT ->R.id.chat
            Menu.EVENTS ->R.id.events
            Menu.SETTINGS ->R.id.settings
        }).isChecked = true
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments.forEach {
            if (it is BackButtonListener && it.backPressed()) {
                return
            }
        }
        presenter.backPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        _view = null
    }
}
