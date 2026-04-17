package com.ihor.thesystem.domain.repository

interface TransactionProvider {
    suspend fun <R> runInTransaction(block: suspend () -> R): R
}
