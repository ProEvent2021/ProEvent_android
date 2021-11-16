package ru.myproevent.ui.screens

import com.github.terrakok.cicerone.Screen
import ru.myproevent.domain.model.entities.Contact

interface IScreens {
    fun authorization(): Screen
    fun home(): Screen
    fun settings(): Screen
    fun settingsList(): Screen
    fun registration(): Screen
    fun code(): Screen
    fun login(): Screen
    fun recovery(): Screen
    fun account(): Screen
    fun security(): Screen
    fun contacts(): Screen
    fun contact(contact: Contact): Screen
    fun contactAdd(): Screen
    fun chat(): Screen
    fun events(): Screen
}