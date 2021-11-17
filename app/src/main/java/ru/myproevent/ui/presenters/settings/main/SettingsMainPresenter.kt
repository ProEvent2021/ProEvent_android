package ru.myproevent.ui.presenters.settings.main

import android.util.Log
import ru.myproevent.ui.presenters.settings.SettingsBasePresenter

class SettingsMainPresenter : SettingsBasePresenter<SettingsMainView>() {
    private var isScreenInitialized = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        if (!isScreenInitialized) {
            isScreenInitialized = true
            Log.d("[MYLOG]", "viewState.init()")
            viewState.init()
        }
    }

    fun showAccount(){
        viewState.showAccount()
    }
}