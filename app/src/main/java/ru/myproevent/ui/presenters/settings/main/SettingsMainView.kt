package ru.myproevent.ui.presenters.settings.main

import moxy.MvpView
import moxy.viewstate.strategy.alias.SingleState

@SingleState
interface SettingsMainView : MvpView{
    fun init()
}