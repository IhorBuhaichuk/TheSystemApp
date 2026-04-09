package com.ihor.thesystem.domain.model

import com.ihor.thesystem.domain.model.Rank

data class MatrixRow(val exerciseId: Int, val exercise: String, val targets: List<String>)

object AnnualMatrixProvider {
    fun getMatrix(): List<MatrixRow> = listOf(
        MatrixRow(8, "Жим від підлоги", listOf("60", "62.5", "65", "65", "67.5", "70", "72.5", "75", "77.5", "80", "80", "82.5", "85")),
        MatrixRow(6, "Присідання", listOf("55", "60", "65", "70", "77.5", "82.5", "87.5", "92.5", "97.5", "105", "110", "115", "120")),
        MatrixRow(12, "Румунська тяга", listOf("60", "65", "72.5", "80", "85", "90", "97.5", "105", "110", "115", "122.5", "130", "135")),
        MatrixRow(13, "Тяга в нахилі", listOf("45", "47.5", "52.5", "55", "57.5", "62.5", "65", "67.5", "72.5", "75", "77.5", "82.5", "85")),
        MatrixRow(9, "Жим гантелей сидячи", listOf("32.5", "34", "36", "38", "40", "42", "44", "46", "48", "49", "51", "53", "55")),
        MatrixRow(11, "Жим гантелей (кут)", listOf("16", "17.5", "19", "20", "21.5", "23", "24", "25.5", "27", "28.5", "30", "31", "32.5")),
        MatrixRow(15, "Згинання біцепс (EZ)", listOf("32", "33", "34", "34.5", "35.5", "36.5", "37", "38", "39", "40", "41", "41.5", "42.5")),
        MatrixRow(16, "Французький жим (EZ)", listOf("22", "23.5", "25", "26.5", "28", "29.5", "31", "32.5", "34", "35.5", "37", "38.5", "40")),
        MatrixRow(14, "Махи гантелями", listOf("6", "6.5", "7.5", "8", "8.5", "9.5", "10", "10.5", "11.5", "12", "12.5", "13.5", "14")),
        MatrixRow(10, "Face Pulls", listOf("8.5", "9.5", "10.5", "11.5", "12.5", "13.5", "14", "15", "16", "17", "18", "19", "20")),
        MatrixRow(7, "Болгарські присідання", listOf("BW", "BW+1.5", "BW+3.5", "BW+5", "BW+6.5", "BW+8.5", "BW+10", "BW+11.5", "BW+13.5", "BW+15", "BW+16.5", "BW+18.5", "BW+20")),
        MatrixRow(5, "Підтягування", listOf("21", "24", "27", "30", "BW+2.5", "BW+3.5", "BW+4.5", "BW+5.5", "BW+6.5", "BW+7.5", "BW+8.5", "BW+9", "BW+10"))
    )

    fun parseTarget(targetStr: String, playerWeight: Double): Double {
        if (targetStr.equals("BW", ignoreCase = true)) return playerWeight
        if (targetStr.uppercase().startsWith("BW+")) {
            val extraWeight = targetStr.uppercase().replace("BW+", "").toDoubleOrNull() ?: 0.0
            return playerWeight + extraWeight
        }
        return targetStr.toDoubleOrNull() ?: 0.0
    }

    /**
     * Повертає ранг вправи на основі exerciseId, 1RM та ваги гравця.
     */
    fun getExerciseRankById(exerciseId: Int, current1RM: Double, playerWeight: Double): Rank {
        val row = getMatrix().find { it.exerciseId == exerciseId } ?: return Rank.E
        val targets = row.targets
        return when {
            targets.size >= 11 && current1RM >= parseTarget(targets[10], playerWeight) -> Rank.S
            targets.size >= 9  && current1RM >= parseTarget(targets[8],  playerWeight) -> Rank.A
            targets.size >= 7  && current1RM >= parseTarget(targets[6],  playerWeight) -> Rank.B
            targets.size >= 5  && current1RM >= parseTarget(targets[4],  playerWeight) -> Rank.C
            targets.size >= 3  && current1RM >= parseTarget(targets[2],  playerWeight) -> Rank.D
            else -> Rank.E
        }
    }

    /**
     * Повертає ранг вправи на основі 1RM та ваги гравця.
     */
    @Deprecated("Use getExerciseRankById", ReplaceWith("getExerciseRankById(exerciseId, current1RM, playerWeight)"))
    fun getExerciseRank(exerciseName: String, current1RM: Double, playerWeight: Double): Rank {
        val row = getMatrix().find { it.exercise.equals(exerciseName, ignoreCase = true) } ?: return Rank.E
        val targets = row.targets

        return when {
            targets.size >= 11 && current1RM >= parseTarget(targets[10], playerWeight) -> Rank.S
            targets.size >= 9 && current1RM >= parseTarget(targets[8], playerWeight) -> Rank.A
            targets.size >= 7 && current1RM >= parseTarget(targets[6], playerWeight) -> Rank.B
            targets.size >= 5 && current1RM >= parseTarget(targets[4], playerWeight) -> Rank.C
            targets.size >= 3 && current1RM >= parseTarget(targets[2], playerWeight) -> Rank.D
            else -> Rank.E
        }
    }
}
