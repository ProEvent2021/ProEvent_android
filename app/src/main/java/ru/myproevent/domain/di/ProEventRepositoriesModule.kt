package ru.myproevent.domain.di

import dagger.Binds
import dagger.Module
import ru.myproevent.domain.model.repositories.internet_access_info.IInternetAccessInfoRepository
import ru.myproevent.domain.model.repositories.proevent_login.IProEventLoginRepository
import ru.myproevent.domain.model.repositories.internet_access_info.InternetAccessInfoRepository
import ru.myproevent.domain.model.repositories.local_proevent_user_token.ITokenLocalRepository
import ru.myproevent.domain.model.repositories.local_proevent_user_token.TokenLocalRepository
import ru.myproevent.domain.model.repositories.proevent_login.ProEventLoginRepository
import ru.myproevent.domain.model.repositories.profiles.IProEventProfilesRepository
import ru.myproevent.domain.model.repositories.profiles.ProEventProfilesRepository

import javax.inject.Singleton

@Module
interface ProEventRepositoriesModule {
    @Singleton
    @Binds
    fun bindLoginRepository(proEventLoginRepository: ProEventLoginRepository): IProEventLoginRepository

    @Singleton
    @Binds
    fun bindInternetAccessInfoRepository(internetAccessInfoRepository: InternetAccessInfoRepository): IInternetAccessInfoRepository

    @Singleton
    @Binds
    fun bindProfilesRepository(proEventProfilesRepository: ProEventProfilesRepository): IProEventProfilesRepository

    @Singleton
    @Binds
    fun bindTokenLocalRepository(tokenLocalRepository: TokenLocalRepository): ITokenLocalRepository
}