package ru.myproevent.ui.presenters.settings.list

import moxy.MvpView
import moxy.viewstate.strategy.alias.SingleState

@SingleState
interface SettingsListView : MvpView{
    fun showAccount()
    fun showSecurity()
}