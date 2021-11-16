package ru.myproevent.ui.presenters.settings

import com.github.terrakok.cicerone.Router
import moxy.MvpView
import ru.myproevent.ui.presenters.BaseMvpPresenter
import javax.inject.Inject
import javax.inject.Named

open class SettingsBasePresenter<V : MvpView>: BaseMvpPresenter<V>() {
    @Inject
    @Named("SettingsRouter")
    lateinit var localRouter: Router

    override fun backPressed(): Boolean {
        localRouter.exit()
        return true
    }
}