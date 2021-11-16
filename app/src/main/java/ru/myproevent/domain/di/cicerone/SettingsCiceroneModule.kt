package ru.myproevent.domain.di.cicerone

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class SettingsCiceroneModule {
    var settingsCicerone: Cicerone<Router> = Cicerone.create()

    @Singleton
    @Provides
    fun provideSettingsCicerone(): Cicerone<Router> = settingsCicerone

    @Singleton
    @Provides
    @Named("SettingsNavigatorHolder")
    fun provideSettingsNavigatorHolder(): NavigatorHolder = settingsCicerone.getNavigatorHolder()

    @Singleton
    @Provides
    @Named("SettingsRouter")
    fun provideSettingsRouter(): Router = settingsCicerone.router
}