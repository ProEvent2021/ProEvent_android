package ru.myproevent.ui.presenters.settings.security

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import ru.myproevent.domain.model.ProfileDto

@AddToEndSingle
interface SecurityView : MvpView {
    fun showProfile(profileDto: ProfileDto)
    fun makeProfileEditable()
    fun showMessage(message: String)
}