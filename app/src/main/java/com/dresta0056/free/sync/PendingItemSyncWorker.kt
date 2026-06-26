package com.dresta0056.free.sync

import android.content.Context
import android.net.Uri
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dresta0056.free.data.LocalItemStore
import com.dresta0056.free.model.toDomain
import com.dresta0056.free.network.AuthSession
import com.dresta0056.free.network.Network
import com.dresta0056.free.network.SessionStore
import com.dresta0056.free.network.toTextPart
import com.dresta0056.free.util.compressToImagePart
import com.dresta0056.free.util.deletePrivateImage
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class PendingItemSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val session = SessionStore(applicationContext).session.first() ?: return Result.retry()
        AuthSession.idToken = session.idToken

        val store = LocalItemStore(applicationContext)
        val pendingItems = store.pendingCreates()
        if (pendingItems.isEmpty()) return Result.success()

        for (pending in pendingItems) {
            if (pending.ownerId != session.profile.id) {
                continue
            }

            try {
                val image = compressToImagePart(applicationContext, Uri.parse(pending.imageUri))
                val remote = Network.api.createItem(
                    title = pending.title.toTextPart(),
                    description = pending.description.toTextPart(),
                    location = pending.location.toTextPart(),
                    contactInfo = pending.contactInfo.toTextPart(),
                    image = image
                ).toDomain()

                store.replacePendingWithRemote(pending.localId, remote)
                deletePrivateImage(pending.imageUri)
            } catch (throwable: Throwable) {
                return when (throwable) {
                    is IOException -> Result.retry()
                    is HttpException -> {
                        if (throwable.code() in 500..599 || throwable.code() == 429) {
                            Result.retry()
                        } else {
                            Result.failure()
                        }
                    }
                    else -> Result.failure()
                }
            }
        }

        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "pending-item-sync"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<PendingItemSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
