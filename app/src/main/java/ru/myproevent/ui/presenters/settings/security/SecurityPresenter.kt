package ru.myproevent.ui.presenters.settings.security

import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import ru.myproevent.domain.model.ProfileDto
import ru.myproevent.domain.model.repositories.internet_access_info.IInternetAccessInfoRepository
import ru.myproevent.domain.model.repositories.proevent_login.IProEventLoginRepository
import ru.myproevent.domain.model.repositories.profiles.IProEventProfilesRepository
import ru.myproevent.ui.presenters.BaseMvpPresenter
import javax.inject.Inject


// TODO: рефакторинг: данный presenter практически копирует AccountPresenter. Возможно стоит вынести общий функционал в абстрактынй класс
class SecurityPresenter : BaseMvpPresenter<SecurityView>() {
    private inner class ProfileEditObserver : DisposableCompletableObserver() {
        override fun onComplete() {
            viewState.showMessage("Изменения сохранены")
        }

        override fun onError(error: Throwable) {
            error.printStackTrace()
            interAccessInfoRepository
                .hasInternetConnection()
                .observeOn(uiScheduler)
                .subscribeWith(InterAccessInfoObserver("Этого не должно было произойти(ProfileEditObserver):\n${error}"))
                .disposeOnDestroy()
        }
    }

    private inner class ProfileGetObserver : DisposableSingleObserver<ProfileDto>() {
        override fun onSuccess(profileDto: ProfileDto) {
            viewState.showProfile(profileDto)
        }

        override fun onError(error: Throwable) {
            error.printStackTrace()
            if (error is retrofit2.adapter.rxjava2.HttpException) {
                when (error.code()) {
                    404 -> viewState.makeProfileEditable()
                }
                return
            }
            interAccessInfoRepository
                .hasInternetConnection()
                .observeOn(uiScheduler)
                .subscribeWith(InterAccessInfoObserver("Этого не должно было произойти (ProfileGetObserver):\n${error}"))
                .disposeOnDestroy()
        }
    }

    @Inject
    lateinit var loginRepository: IProEventLoginRepository

    @Inject
    lateinit var profilesRepository: IProEventProfilesRepository

    @Inject
    lateinit var interAccessInfoRepository: IInternetAccessInfoRepository

    fun saveProfile(email: String, login: String, password: String) {
        profilesRepository
            .saveProfile(
                ProfileDto(
                    userId = loginRepository.getLocalId()!!,
                    nickName = login
                )
            )
            .observeOn(uiScheduler)
            .subscribeWith(ProfileEditObserver())
            .disposeOnDestroy()
    }

    fun getProfile() {
        profilesRepository
            .getProfile(loginRepository.getLocalId()!!)
            .observeOn(uiScheduler)
            .subscribeWith(ProfileGetObserver())
            .disposeOnDestroy()
    }
}