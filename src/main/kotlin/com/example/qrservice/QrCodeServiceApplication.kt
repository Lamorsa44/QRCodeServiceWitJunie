package com.example.qrservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QrCodeServiceApplication

fun main(args: Array<String>) {
    runApplication<QrCodeServiceApplication>(*args)
}
