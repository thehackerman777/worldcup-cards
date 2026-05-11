package com.wcapp.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WorldCupCardsApplication

fun main(args: Array<String>) {
    runApplication<WorldCupCardsApplication>(*args)
}
