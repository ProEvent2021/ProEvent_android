package ru.myproevent.ui.presenters.settings.main

import ru.myproevent.ui.presenters.settings.SettingsBasePresenter

class SettingsMainPresenter : SettingsBasePresenter<SettingsMainView>() {
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        localRouter.replaceScreen(screens.settingsList())
    }
}