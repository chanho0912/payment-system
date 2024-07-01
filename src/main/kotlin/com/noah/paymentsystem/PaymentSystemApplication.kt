package com.noah.paymentsystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class PaymentSystemApplication

fun main(args: Array<String>) {
    runApplication<PaymentSystemApplication>(*args)
}
