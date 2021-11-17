package ru.myproevent.ui.presenters.settings.list

import ru.myproevent.domain.model.repositories.proevent_login.IProEventLoginRepository
import ru.myproevent.ui.presenters.settings.SettingsBasePresenter
import javax.inject.Inject

class SettingsListPresenter : SettingsBasePresenter<SettingsListView>() {
    @Inject
    lateinit var loginRepository: IProEventLoginRepository

    fun account() {
        viewState.showAccount()
    }

    fun security() {
        viewState.showSecurity()
    }

    fun logout() {
        loginRepository.logoutFromThisDevice()
        router.newRootScreen(screens.authorization())
    }
}