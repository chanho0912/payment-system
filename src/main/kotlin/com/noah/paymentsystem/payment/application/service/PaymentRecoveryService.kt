package com.noah.paymentsystem.payment.application.service

import com.noah.paymentsystem.common.Usecase
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmCommand
import com.noah.paymentsystem.payment.application.port.`in`.PaymentRecoveryUsecase
import com.noah.paymentsystem.payment.application.port.out.LoadPendingPaymentPort
import com.noah.paymentsystem.payment.application.port.out.PaymentExecutorPort
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdateCommand
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdatePort
import com.noah.paymentsystem.payment.application.port.out.PaymentValidationPort
import org.springframework.scheduling.annotation.Scheduled
import reactor.core.scheduler.Schedulers
import java.util.concurrent.TimeUnit

@Usecase
class PaymentRecoveryService(
    private val loadPendingPaymentPort: LoadPendingPaymentPort,
    private val paymentValidationPort: PaymentValidationPort,
    private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    private val paymentExecutorPort: PaymentExecutorPort
) : PaymentRecoveryUsecase {

    private val scheduler = Schedulers.newSingle("recovery-scheduler")

    @Scheduled(fixedDelay = 180, initialDelay = 180, timeUnit = TimeUnit.SECONDS)
    override fun recovery() {
        loadPendingPaymentPort.getPendingPayment()
            .map {
                PaymentConfirmCommand(
                    paymentKey = it.paymentKey,
                    orderId = it.orderId,
                    amount = it.totalAmount()
                )
            }
            .parallel(2)
            .runOn(Schedulers.parallel())
            .flatMap { command ->
                paymentValidationPort.isValid(command.orderId, command.amount).thenReturn(command)
                    .flatMap { paymentExecutorPort.execute(it) }
                    .flatMap { paymentStatusUpdatePort.updatePaymentStatus(PaymentStatusUpdateCommand(it))  }
            }
            .sequential()
            .subscribeOn(scheduler)
            .subscribe()
    }
}
