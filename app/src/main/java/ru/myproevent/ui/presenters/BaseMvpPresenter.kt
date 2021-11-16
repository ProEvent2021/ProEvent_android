package ru.myproevent.ui.presenters

import android.widget.Toast
import com.github.terrakok.cicerone.Router
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import moxy.MvpPresenter
import moxy.MvpView
import ru.myproevent.ProEventApp
import ru.myproevent.ui.screens.IScreens
import javax.inject.Inject
import javax.inject.Named

open class BaseMvpPresenter<V : MvpView> : MvpPresenter<V>() {

    @Inject
    lateinit var uiScheduler: Scheduler

    @Inject
    @Named("MainRouter")
    open lateinit var router: Router

    @Inject
    lateinit var screens: IScreens

    private var compositeDisposable = CompositeDisposable()

    protected inner class InterAccessInfoObserver(private val onAccessErrorMessage: String?): DisposableSingleObserver<Boolean>() {
        override fun onSuccess(hasInternetAccess: Boolean) {
            if(!hasInternetAccess){
                Toast.makeText(ProEventApp.instance, "У устройства нет выхода в Интернет.\n(От Google Public DNS нет ответа)", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(ProEventApp.instance, "ERROR_MESSAGE: $onAccessErrorMessage", Toast.LENGTH_LONG).show()
            }
        }

        override fun onError(error: Throwable) {
            error.printStackTrace()
            Toast.makeText(ProEventApp.instance, "${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    protected fun Disposable.disposeOnDestroy() {
        compositeDisposable.add(this)
    }

    open fun backPressed(): Boolean {
        router.exit()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}