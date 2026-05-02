package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.ViewingDateRepository
import java.time.LocalDate
import javax.inject.Inject

class SelectViewingDateUseCase @Inject constructor(
    private val viewingDateRepository: ViewingDateRepository
) {
    operator fun invoke(date: LocalDate?) {
        viewingDateRepository.setDate(date)
    }
}
