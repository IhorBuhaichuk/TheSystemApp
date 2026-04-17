package com.ihor.thesystem.data.local.room

import androidx.room.withTransaction
import com.ihor.thesystem.data.local.room.database.AppDatabase
import com.ihor.thesystem.domain.repository.TransactionProvider
import javax.inject.Inject

class TransactionProviderImpl @Inject constructor(
    private val db: AppDatabase
) : TransactionProvider {
    override suspend fun <R> runInTransaction(block: suspend () -> R): R {
        return db.withTransaction {
            block()
        }
    }
}
