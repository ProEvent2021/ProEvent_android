package ru.myproevent.ui.presenters.settings.main

import ru.myproevent.domain.model.repositories.internet_access_info.IInternetAccessInfoRepository
import ru.myproevent.ui.presenters.BaseMvpPresenter
import ru.myproevent.ui.presenters.settings.SettingsBasePresenter
import javax.inject.Inject

class SettingsMainPresenter : BaseMvpPresenter<SettingsMainView>() {
//    @Inject
//    @Named("SettingsRouter")
//    lateinit var localRouter: Router

    @Inject
    lateinit var interAccessInfoRepository: IInternetAccessInfoRepository

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        router.replaceScreen(screens.settingsList())
    }
}