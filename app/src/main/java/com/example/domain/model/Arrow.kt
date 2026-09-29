package com.example.domain.model

data class Arrow(
    val id: Int,
    val points: List<GridPoint>,
    val customDirection: Direction? = null
) {
    init {
        require(points.size >= 2) { "Arrow must have at least 2 points (tail and tip)" }
    }

    val tip: GridPoint
        get() = points.last()

    val tail: GridPoint
        get() = points.first()

    val direction: Direction
        get() {
            if (customDirection != null) return customDirection
            val pPrev = points[points.size - 2]
            val pLast = points.last()
            val dx = pLast.x - pPrev.x
            val dy = pLast.y - pPrev.y
            return Direction.fromDelta(dx, dy)
        }

    /**
     * All discrete grid integer coordinates covered by this arrow's path segments.
     */
    val coveredGridPoints: Set<GridPoint> by lazy {
        val result = mutableSetOf<GridPoint>()
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val minX = minOf(p1.x, p2.x)
            val maxX = maxOf(p1.x, p2.x)
            val minY = minOf(p1.y, p2.y)
            val maxY = maxOf(p1.y, p2.y)
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    result.add(GridPoint(x, y))
                }
            }
        }
        result
    }

    /**
     * Check if a given line segment (x1, y1) -> (x2, y2) intersects with any segment of this arrow.
     */
    fun intersectsRay(from: GridPoint, dir: Direction, maxBound: Int): Boolean {
        // Steps along dir starting from `from`
        var cur = from
        for (step in 1..maxBound) {
            cur += dir
            if (coveredGridPoints.contains(cur)) {
                return true
            }
        }
        return false
    }
}
