package com.ihor.thesystem.domain.util

import com.ihor.thesystem.domain.model.DomainError

/**
 * Спеціальне виключення для примусового відкату транзакції Room.
 * Room робить rollback тільки при виникненні Exception.
 */
class TransactionRollbackException(val error: DomainError) : Exception("Transaction rollback due to: $error")
