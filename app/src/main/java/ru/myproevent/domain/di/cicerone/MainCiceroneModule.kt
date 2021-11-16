package ru.myproevent.domain.di.cicerone

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import dagger.Module
import dagger.Provides
import ru.myproevent.ui.screens.IScreens
import ru.myproevent.ui.screens.Screens
import javax.inject.Named
import javax.inject.Singleton

@Module
class MainCiceroneModule {
    var mainCicerone: Cicerone<Router> = Cicerone.create()

    @Singleton
    @Provides
    fun provideMainCicerone(): Cicerone<Router> = mainCicerone

    @Singleton
    @Provides
    @Named("MainNavigatorHolder")
    fun provideMainNavigatorHolder(): NavigatorHolder = mainCicerone.getNavigatorHolder()

    @Singleton
    @Provides
    @Named("MainRouter")
    fun provideMainRouter(): Router = mainCicerone.router

    @Singleton
    @Provides
    fun provideScreens(): IScreens = Screens()
}