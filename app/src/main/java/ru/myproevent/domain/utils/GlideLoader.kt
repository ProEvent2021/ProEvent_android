package ru.myproevent.domain.utils

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import ru.myproevent.BuildConfig
import ru.myproevent.domain.models.repositories.proevent_login.IProEventLoginRepository
import javax.inject.Inject

class GlideLoader {

    @Inject
    lateinit var loginRepository: IProEventLoginRepository

    fun loadCircle(imageView: ImageView, uri: Uri) {
        Glide.with(imageView.context)
            .load(uri)
            .circleCrop()
            .into(imageView)
    }

    fun loadCircle(imageView: ImageView, uuid: String) {
        Glide.with(imageView.context)
            .load(getGlideUrl(uuid))
            .circleCrop()
            .into(imageView)
    }

    private fun getGlideUrl(uuid: String): GlideUrl {
        return GlideUrl(
            "$URL_PATH$uuid",
            LazyHeaders.Builder()
                .addHeader(HEADER_KEY, "$HEADER_VALUE ${loginRepository.getLocalToken()}")
                .build()
        )
    }

    companion object {
        const val URL_PATH = "${BuildConfig.PROEVENT_API_URL}storage/"
        const val HEADER_KEY = "Authorization"
        const val HEADER_VALUE = "Bearer"
    }
}
