package com.noah.paymentsystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.scheduling.annotation.EnableScheduling
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Function

@EnableScheduling
@SpringBootApplication
class PaymentSystemApplication {

    @Bean
    fun consume(): Function<Flux<Message<String>>, Mono<Void>> {
        return Function { messages ->
            messages
                .map { message ->
                    println("Consumed message : $message")
                    message
                }
                .then()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<PaymentSystemApplication>(*args)
}
