package com.example.domain.model

data class GridPoint(val x: Int, val y: Int) {
    operator fun plus(other: GridPoint) = GridPoint(x + other.x, y + other.y)
    operator fun plus(dir: Direction) = GridPoint(x + dir.dx, y + dir.dy)
    operator fun minus(other: GridPoint) = GridPoint(x - other.x, y - other.y)
}
