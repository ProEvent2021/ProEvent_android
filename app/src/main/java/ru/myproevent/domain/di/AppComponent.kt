package ru.myproevent.domain.di

import dagger.Component
import moxy.MvpView
import ru.myproevent.domain.di.cicerone.MainCiceroneModule
import ru.myproevent.domain.di.cicerone.SettingsCiceroneModule
import ru.myproevent.ui.activity.MainActivity
import ru.myproevent.ui.fragments.settings.SettingsListListFragment
import ru.myproevent.ui.fragments.settings.SettingsMainFragment
import ru.myproevent.ui.presenters.BaseMvpPresenter
import ru.myproevent.ui.presenters.settings.account.AccountPresenter
import ru.myproevent.ui.presenters.authorization.AuthorizationPresenter
import ru.myproevent.ui.presenters.code.CodePresenter
import ru.myproevent.ui.presenters.contact_add.ContactAddPresenter
import ru.myproevent.ui.presenters.contacts.ContactsPresenter
import ru.myproevent.ui.presenters.home.HomePresenter
import ru.myproevent.ui.presenters.login.LoginPresenter
import ru.myproevent.ui.presenters.main.MainPresenter
import ru.myproevent.ui.presenters.recovery.RecoveryPresenter
import ru.myproevent.ui.presenters.registration.RegistrationPresenter
import ru.myproevent.ui.presenters.settings.SettingsBasePresenter
import ru.myproevent.ui.presenters.settings.security.SecurityPresenter
import ru.myproevent.ui.presenters.settings.list.SettingsListPresenter
import ru.myproevent.ui.presenters.settings.main.SettingsMainPresenter
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        MainCiceroneModule::class,
        SettingsCiceroneModule::class,
        PresentersModule::class,
        ProEventApiModule::class,
        ProEventRepositoriesModule::class
    ]
)
interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(settingsMainFragment: SettingsMainFragment)
    fun inject(settingsListListFragment: SettingsListListFragment)

    //fun inject(baseMvpPresenter: BaseMvpPresenter<MvpView>)
    //fun inject(settingsBasePresenter: SettingsBasePresenter<MvpView>)

    fun inject(mainPresenter: MainPresenter)
    fun inject(settingsMainPresenter: SettingsMainPresenter)

    fun inject(authorizationPresenter: AuthorizationPresenter)
    fun inject(codePresenter: CodePresenter)
    fun inject(homePresenter: HomePresenter)
    fun inject(loginPresenter: LoginPresenter)
    fun inject(recoveryPresenter: RecoveryPresenter)
    fun inject(registrationPresenter: RegistrationPresenter)
    fun inject(settingsListPresenter: SettingsListPresenter)
    fun inject(accountPresenter: AccountPresenter)
    fun inject(securityPresenter: SecurityPresenter)
    fun inject(contactsPresenter: ContactsPresenter)
    fun inject(contactAddPresenter: ContactAddPresenter)
}