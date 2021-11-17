package ru.myproevent.domain.di

import dagger.Module
import dagger.Provides
import ru.myproevent.ProEventApp
import ru.myproevent.ui.presenters.settings.list.SettingsListPresenter
import ru.myproevent.ui.presenters.settings.main.SettingsMainPresenter
import javax.inject.Singleton

@Module
class PresentersModule {
    @Singleton
    @Provides
    fun provideSettingsMainPresenter(): SettingsMainPresenter = SettingsMainPresenter().apply { ProEventApp.instance.appComponent.inject(this) }

    @Singleton
    @Provides
    fun provideSettingsListPresenter(): SettingsListPresenter = SettingsListPresenter().apply { ProEventApp.instance.appComponent.inject(this) }
}