package ru.myproevent.ui.presenters.settings.main

import com.github.terrakok.cicerone.Router
import ru.myproevent.ui.presenters.settings.SettingsBasePresenter
import javax.inject.Inject
import javax.inject.Named

class SettingsMainPresenter : SettingsBasePresenter<SettingsMainView>() {
//    @Inject
//    @Named("SettingsRouter")
//    lateinit var localRouter: Router

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        router.replaceScreen(screens.settingsList())
    }
}