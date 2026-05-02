package com.ihor.thesystem.domain.util

object LinearInterpolator {
    fun interpolate(value: Double, points: List<Pair<Double, Double>>): Double {
        require(points.size >= 2) {
            "At least two interpolation points are required"
        }

        val sortedPoints = points.sortedBy { it.first }
        val firstPoint = sortedPoints.first()
        val lastPoint = sortedPoints.last()

        if (value <= firstPoint.first) return firstPoint.second
        if (value >= lastPoint.first) return lastPoint.second

        val upperIndex = sortedPoints.indexOfFirst { it.first >= value }
        val lowerPoint = sortedPoints[upperIndex - 1]
        val upperPoint = sortedPoints[upperIndex]
        val range = upperPoint.first - lowerPoint.first

        if (range == 0.0) return upperPoint.second

        val ratio = (value - lowerPoint.first) / range
        return lowerPoint.second + (upperPoint.second - lowerPoint.second) * ratio
    }
}
