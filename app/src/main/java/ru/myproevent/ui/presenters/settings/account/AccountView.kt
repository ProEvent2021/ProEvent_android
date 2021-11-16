package ru.myproevent.ui.presenters.settings.account

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.myproevent.domain.model.ProfileDto

// TODO: возможно стоит выбрать другую стратегию
@StateStrategyType(AddToEndStrategy::class)
interface AccountView: MvpView{
    fun showProfile(profileDto: ProfileDto)
    fun makeProfileEditable()
    fun showMessage(message: String)
}
