package com.gmail.shellljx.pixelate.extension

import android.graphics.Bitmap
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.gmail.shellljx.pixelate.utils.DensityUtils
import com.gmail.shellljx.wrapper.IContainer
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import kotlin.reflect.KClass

fun Int.dp(): Int {
    return DensityUtils.dip2px(this.toFloat())
}

fun Float.dp(): Int {
    return DensityUtils.dip2px(this)
}

fun launch(container: IContainer, block: suspend CoroutineScope.() -> Unit) {
    container.getLifeCycleService()?.launchSafely(block = block)
}

suspend fun Bitmap.writeToPngFile(targetPath: String, quality: Int): Boolean {
    return withContext(Dispatchers.IO) {
        val targetFile = File(targetPath)
        if (targetFile.parentFile?.exists() != true) {
            targetFile.parentFile?.mkdirs()
        }
        val stream = FileOutputStream(targetFile)
        try {
            compress(Bitmap.CompressFormat.PNG, quality, stream)
        } catch (e: Exception) {
            false
        } finally {
            stream.flush()
            stream.close()
        }
    }
}

@MainThread
inline fun <T, reified VM : ViewModel> T.viewModels(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
) where T : ViewModelStoreOwner, T : LifecycleOwner = createViewModelLazy(VM::class, { ownerProducer().viewModelStore }, factoryProducer)

@MainThread
fun <VM : ViewModel> createViewModelLazy(
    viewModelClass: KClass<VM>,
    owner: ViewModelStoreOwner,
    factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: { getDefaultViewModelProviderFactory(owner) }
    return ViewModelLazy(viewModelClass, { owner.viewModelStore }, factoryPromise)
}

@MainThread
fun getDefaultViewModelProviderFactory(owner: ViewModelStoreOwner): ViewModelProvider.Factory =
    if (owner is HasDefaultViewModelProviderFactory) owner.defaultViewModelProviderFactory else ViewModelProvider.NewInstanceFactory()