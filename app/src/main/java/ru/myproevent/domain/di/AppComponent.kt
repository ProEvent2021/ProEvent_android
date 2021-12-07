package ru.myproevent.domain.di

import dagger.Component
import moxy.MvpView
import ru.myproevent.ui.activity.BottomNavigationActivity
import ru.myproevent.ui.fragments.TabContainerFragment
import ru.myproevent.ui.presenters.BaseMvpPresenter
import ru.myproevent.ui.presenters.settings.account.AccountPresenter
import ru.myproevent.ui.presenters.authorization.authorization.AuthorizationPresenter
import ru.myproevent.ui.presenters.authorization.code.CodePresenter
import ru.myproevent.ui.presenters.contacts.contact_add.ContactAddPresenter
import ru.myproevent.ui.presenters.contacts.contacts_list.ContactsPresenter
import ru.myproevent.ui.presenters.home.HomePresenter
import ru.myproevent.ui.presenters.authorization.login.LoginPresenter
import ru.myproevent.ui.presenters.main.BottomNavigationPresenter
import ru.myproevent.ui.presenters.authorization.recovery.RecoveryPresenter
import ru.myproevent.ui.presenters.authorization.registration.RegistrationPresenter
import ru.myproevent.ui.presenters.events.event.EventPresenter
import ru.myproevent.ui.presenters.events.EventsPresenter
import ru.myproevent.ui.presenters.events.event.confirmation.EventActionConfirmPresenter
import ru.myproevent.ui.presenters.events.event.participant_pickers.ParticipantPickerTypeSelectionPresenter
import ru.myproevent.ui.presenters.events.event.participant_pickers.participant_from_contacts_picker.ParticipantFromContactsPickerPresenter
import ru.myproevent.ui.presenters.settings.security.SecurityPresenter
import ru.myproevent.ui.presenters.settings.settings_list.SettingsPresenter
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        CiceroneModule::class,
        LocalNavigationModule::class,
        ProEventApiModule::class,
        ProEventRepositoriesModule::class
    ]
)
interface AppComponent {
    fun inject(bottomNavigationActivity: BottomNavigationActivity)

    fun inject(tabContainerFragment: TabContainerFragment)

    fun inject(baseMvpPresenter: BaseMvpPresenter<MvpView>)
    fun inject(bottomNavigationPresenter: BottomNavigationPresenter)
    fun inject(authorizationPresenter: AuthorizationPresenter)
    fun inject(codePresenter: CodePresenter)
    fun inject(homePresenter: HomePresenter)
    fun inject(loginPresenter: LoginPresenter)
    fun inject(recoveryPresenter: RecoveryPresenter)
    fun inject(registrationPresenter: RegistrationPresenter)
    fun inject(settingsPresenter: SettingsPresenter)
    fun inject(accountPresenter: AccountPresenter)
    fun inject(securityPresenter: SecurityPresenter)
    fun inject(contactsPresenter: ContactsPresenter)
    fun inject(contactAddPresenter: ContactAddPresenter)
    fun inject(eventsPresenter: EventsPresenter)
    fun inject(eventPresenter: EventPresenter)
    fun inject(eventActionConfirmPresenter: EventActionConfirmPresenter)
    fun inject(participantPickerTypeSelectionPresenter: ParticipantPickerTypeSelectionPresenter)
    fun inject(participantFromContactsPickerPresenter: ParticipantFromContactsPickerPresenter)
}