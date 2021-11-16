package ru.myproevent.ui.presenters.main

import android.widget.Toast
import ru.myproevent.ProEventApp
import ru.myproevent.domain.model.repositories.proevent_login.IProEventLoginRepository
import ru.myproevent.ui.presenters.BaseMvpPresenter
import javax.inject.Inject

class MainPresenterOld : BaseMvpPresenter<MainView>() {
    @Inject
    lateinit var loginRepository: IProEventLoginRepository

    private var currActiveMenu = Menu.HOME

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        if(loginRepository.getLocalToken() == null){
            router.replaceScreen(screens.authorization())
        } else {
            router.replaceScreen(screens.home())
        }

    }

    fun itemSelected(menu: Menu) {
        currActiveMenu = menu
    }

    fun openHome() {
        if (currActiveMenu == Menu.HOME) {
            return
        }
        currActiveMenu = Menu.HOME
        router.navigateTo(screens.home())
    }

    fun openContacts() {
        if (currActiveMenu == Menu.CONTACTS) {
            return
        }
        currActiveMenu = Menu.CONTACTS
        router.navigateTo(screens.contacts())
    }

    fun openChat() {
        Toast.makeText(ProEventApp.instance, "CHAT", Toast.LENGTH_LONG).show()
    }

    fun openEvents() {
        Toast.makeText(ProEventApp.instance, "EVENTS", Toast.LENGTH_LONG).show()
    }

    fun openSettings() {
        if (currActiveMenu == Menu.SETTINGS) {
            return
        }
        currActiveMenu = Menu.SETTINGS
        router.navigateTo(screens.settingsList())
    }
}