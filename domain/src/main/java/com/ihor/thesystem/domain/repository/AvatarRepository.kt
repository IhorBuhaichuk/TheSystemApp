package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.model.DataError

interface AvatarRepository {
    suspend fun saveAvatar(sourceUri: String): Result<String, DataError.Local>
}
