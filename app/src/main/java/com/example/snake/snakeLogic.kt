package com.example.snake.game

import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

enum class Direction { UP, DOWN, LEFT, RIGHT }

data class Point(val x: Int, val y: Int)

data class GameState(
    val snake: List<Point> = listOf(Point(10, 10), Point(10, 11), Point(10, 12)),
    val food: Point = Point(5, 5),
    val direction: Direction = Direction.UP,
    val isGameOver: Boolean = false,
    val currentScore: Int = 0
) {
    companion object {
        const val BOARD_SIZE = 20
    }

    fun move(): GameState {
        if (isGameOver) return this

        val head = snake.first()
        val newHead = when (direction) {
            Direction.UP -> Point(head.x, (head.y - 1 + BOARD_SIZE) % BOARD_SIZE)
            Direction.DOWN -> Point(head.x, (head.y + 1) % BOARD_SIZE)
            Direction.LEFT -> Point((head.x - 1 + BOARD_SIZE) % BOARD_SIZE, head.y)
            Direction.RIGHT -> Point((head.x + 1) % BOARD_SIZE, head.y)
        }

        // Crash Prevention & Rule Check: Self-collision
        if (snake.contains(newHead)) {
            return this.copy(isGameOver = true)
        }

        val newSnake = mutableListOf(newHead).apply { addAll(snake) }

        return if (newHead == food) {
            // Snake eats food
            var newFood = Point(Random.nextInt(BOARD_SIZE), Random.nextInt(BOARD_SIZE))
            while (newSnake.contains(newFood)) {
                newFood = Point(Random.nextInt(BOARD_SIZE), Random.nextInt(BOARD_SIZE))
            }
            this.copy(snake = newSnake, food = newFood, currentScore = currentScore + 10)
        } else {
            // Just moving forward, remove tail piece
            newSnake.removeAt(newSnake.lastIndex)
            this.copy(snake = newSnake)
        }
    }
}