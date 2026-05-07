package com.ihor.thesystem.data.repository_impl

import android.content.Context
import android.net.Uri
import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.repository.AvatarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import timber.log.Timber

class AvatarRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider
) : AvatarRepository {

    override suspend fun saveAvatar(sourceUri: String): Result<String, DataError.Local> =
        withContext(dispatchers.io) {
            try {
                val avatarDir = File(context.filesDir, AVATAR_DIR)
                if (!avatarDir.exists() && !avatarDir.mkdirs()) {
                    return@withContext Result.Error(DataError.Local.UNKNOWN)
                }

                avatarDir.listFiles()?.forEach { file ->
                    if (!file.delete()) {
                        Timber.w("Failed to delete old avatar file: ${file.name}")
                    }
                }

                val destFile = File(avatarDir, "avatar_${clock.now()}.jpg")
                val source = Uri.parse(sourceUri)

                context.contentResolver.openInputStream(source)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext Result.Error(DataError.Local.NOT_FOUND)

                Result.Success(Uri.fromFile(destFile).toString())
            } catch (e: SecurityException) {
                Timber.e(e, "Avatar source is not accessible")
                Result.Error(DataError.Local.NOT_FOUND)
            } catch (e: IOException) {
                Timber.e(e, "Failed to copy avatar to internal storage")
                Result.Error(DataError.Local.UNKNOWN)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Unexpected avatar save error")
                Result.Error(DataError.Local.UNKNOWN)
            }
        }

    private companion object {
        const val AVATAR_DIR = "avatars"
    }
}
