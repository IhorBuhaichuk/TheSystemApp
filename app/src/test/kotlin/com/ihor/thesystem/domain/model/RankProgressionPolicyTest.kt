package com.ihor.thesystem.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RankProgressionPolicyTest {

    @Test
    fun `next rank promotes one step and caps at S`() {
        assertEquals(Rank.D, RankProgressionPolicy.nextRank(Rank.E))
        assertEquals(Rank.S, RankProgressionPolicy.nextRank(Rank.A))
        assertEquals(Rank.S, RankProgressionPolicy.nextRank(Rank.S))
    }

    @Test
    fun `candidate rank promotes only when it is higher`() {
        assertTrue(RankProgressionPolicy.shouldPromote(current = Rank.C, candidate = Rank.B))
        assertFalse(RankProgressionPolicy.shouldPromote(current = Rank.C, candidate = Rank.C))
        assertFalse(RankProgressionPolicy.shouldPromote(current = Rank.C, candidate = Rank.D))
    }

    @Test
    fun `global rank is median of top five exercise ranks`() {
        val result = RankProgressionPolicy.resolveGlobalRank(
            ranks = listOf(Rank.E, Rank.D, Rank.C, Rank.B, Rank.A, Rank.S)
        )

        assertEquals(Rank.B, result)
    }

    @Test
    fun `global rank is absent when no exercise ranks exist`() {
        assertNull(RankProgressionPolicy.resolveGlobalRank(emptyList()))
    }
}
