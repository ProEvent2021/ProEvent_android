package ru.myproevent.ui.presenters.settings.list

import android.util.Log
import ru.myproevent.domain.model.repositories.proevent_login.IProEventLoginRepository
import ru.myproevent.ui.presenters.BaseMvpPresenter
import ru.myproevent.ui.presenters.settings.SettingsBasePresenter
import javax.inject.Inject

class SettingsListPresenter : SettingsBasePresenter<SettingsListView>() {
    @Inject
    lateinit var loginRepository: IProEventLoginRepository

    fun account() {
        Log.d("[MYLOG]", "localRouter.navigateTo(screens.account())")
        localRouter.navigateTo(screens.account())
    }

    fun security() {
        localRouter.navigateTo(screens.security())
    }

    fun logout() {
        loginRepository.logoutFromThisDevice()
        router.newRootScreen(screens.authorization())
    }
}