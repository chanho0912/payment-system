package com.noah.paymentsystem

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcConnectionDetails
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import java.net.InetAddress

@EnableScheduling
@SpringBootApplication
class PaymentSystemApplication

fun main(args: Array<String>) {
    runApplication<PaymentSystemApplication>(*args)
}
