package com.noah.paymentsystem.payment.adapter.out.stream

import com.noah.paymentsystem.common.Logger
import com.noah.paymentsystem.common.StreamAdapter
import com.noah.paymentsystem.payment.adapter.out.persistence.repository.PaymentOutboxRepository
import com.noah.paymentsystem.payment.application.domain.PaymentEventMessage
import com.noah.paymentsystem.payment.application.domain.PaymentEventMessageType
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.IntegrationMessageHeaderAccessor
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.FluxMessageChannel
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import reactor.kafka.sender.SenderResult
import java.util.function.Supplier

@Configuration
@StreamAdapter
class PaymentEventMessageSender(
    private val paymentOutboxRepository: PaymentOutboxRepository
) {

    private val sender =
        Sinks.many().unicast().onBackpressureBuffer<Message<PaymentEventMessage>>()
    private val sendResult =
        Sinks.many().unicast().onBackpressureBuffer<SenderResult<String>>()

    @Bean(name = ["payment-result"])
    fun sendResultChannel(): FluxMessageChannel {
        return FluxMessageChannel()
    }

    @ServiceActivator(inputChannel = "payment-result")
    fun sendResult(results: SenderResult<String>) {
        if (results.exception() != null) {
            Logger.error(
                "sendEventMessage",
                results.exception().message ?: "receive an exception for event message send",
                results.exception()
            )
        }

        sendResult.emitNext(results, Sinks.EmitFailureHandler.FAIL_FAST)
    }

    @PostConstruct
    fun handleSendResult() {
        sendResult.asFlux()
            .flatMap {
                when (it.recordMetadata() != null) {
                    true -> paymentOutboxRepository.markMessageAsSent(it.correlationMetadata(), PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS)
                    false -> paymentOutboxRepository.markMessageAsFailure(it.correlationMetadata(), PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS)
                }
            }
            .onErrorContinue { err, _ -> Logger.error("sendEventMessage", err.message ?: "failed to mark the outbox message.", err)  }
            .subscribeOn(
                Schedulers
                    .newSingle("handle-send-result-event-message")
            )
            .subscribe()
    }

    @Bean
    fun send(): Supplier<Flux<Message<PaymentEventMessage>>> {
        return Supplier {
            sender.asFlux()
                .onErrorContinue { err, _ ->
                    Logger.error(
                        "sendEventMessage",
                        err.message ?: "failed to send eventMessage",
                        err
                    )
                }
        }
    }

    fun dispatch(paymentEventMessage: PaymentEventMessage) {
        val message = buildMessage(paymentEventMessage)

        sender.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST)
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    fun dispatchAfterCommit(paymentEventMessage: PaymentEventMessage) {
        val message = buildMessage(paymentEventMessage)

        sender.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST)
    }

    private fun buildMessage(paymentEventMessage: PaymentEventMessage) =
        MessageBuilder
            .withPayload(paymentEventMessage)
            .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, paymentEventMessage.payload["orderId"])
            .setHeader(KafkaHeaders.PARTITION, paymentEventMessage.metadata["partitionKey"] ?: 0)
            .build()
}
