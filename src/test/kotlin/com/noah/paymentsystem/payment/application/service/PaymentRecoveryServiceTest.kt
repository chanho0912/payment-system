package com.noah.paymentsystem.payment.application.service

import com.noah.paymentsystem.payment.application.domain.PSPConfirmationStatus.DONE
import com.noah.paymentsystem.payment.application.domain.PaymentFailure
import com.noah.paymentsystem.payment.application.domain.PaymentMethod.EASY_PAY
import com.noah.paymentsystem.payment.application.domain.PaymentStatus.UNKNOWN
import com.noah.paymentsystem.payment.application.domain.PaymentType.NORMAL
import com.noah.paymentsystem.payment.application.port.`in`.CheckoutCommand
import com.noah.paymentsystem.payment.application.port.`in`.CheckoutUsecase
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmCommand
import com.noah.paymentsystem.payment.application.port.out.LoadPendingPaymentPort
import com.noah.paymentsystem.payment.application.port.out.PaymentExecutionResult
import com.noah.paymentsystem.payment.application.port.out.PaymentExecutorPort
import com.noah.paymentsystem.payment.application.port.out.PaymentExtraDetails
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdateCommand
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdatePort
import com.noah.paymentsystem.payment.application.port.out.PaymentValidationPort
import com.noah.paymentsystem.payment.test.PaymentDatabaseHelper
import com.noah.paymentsystem.payment.test.PaymentTestConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
@Import(PaymentTestConfiguration::class)
class PaymentRecoveryServiceTest(
    @Autowired private val loadPendingPaymentPort: LoadPendingPaymentPort,
    @Autowired private val paymentValidationPort: PaymentValidationPort,
    @Autowired private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    @Autowired private val checkoutUsecase: CheckoutUsecase,
    @Autowired private val paymentDatabaseHelper: PaymentDatabaseHelper
) {

    @BeforeEach
    fun clean() {
        paymentDatabaseHelper.clean().block()
    }

    @Test
    fun `should recovery payments`() {
        val paymentConfirmCommand = createUnknownStatusPayment()
        val paymentExecutionResult = createPaymentExecutionResult(paymentConfirmCommand)
        val mockPaymentExecutorPort = mockk<PaymentExecutorPort>()
        every {
            mockPaymentExecutorPort.execute(paymentConfirmCommand)
        } returns Mono.just(
            paymentExecutionResult
        )

        val paymentRecoveryService = PaymentRecoveryService(
            loadPendingPaymentPort = loadPendingPaymentPort,
            paymentValidationPort = paymentValidationPort,
            paymentStatusUpdatePort = paymentStatusUpdatePort,
            paymentExecutorPort = mockPaymentExecutorPort
        )

        paymentRecoveryService.recovery()

        Thread.sleep(10000)
    }

    private fun createPaymentExecutionResult(paymentConfirmCommand: PaymentConfirmCommand) =
        PaymentExecutionResult(
            orderId = paymentConfirmCommand.orderId,
            paymentKey = paymentConfirmCommand.paymentKey,
            extraDetails = PaymentExtraDetails(
                type = NORMAL,
                method = EASY_PAY,
                totalAmount = paymentConfirmCommand.amount,
                orderName = "test_order_name",
                pspConfirmationStatus = DONE,
                approvedAt = LocalDateTime.now(),
                pspRawData = "{}"
            ),
            isSuccess = true,
            isFailure = false,
            isUnknown = false,
            isRetryable = false
        )


    private fun createUnknownStatusPayment(): PaymentConfirmCommand {
        val orderId = UUID.randomUUID().toString()
        val paymentKey = UUID.randomUUID().toString()

        val checkoutCommand = CheckoutCommand(
            cartId = 1,
            buyerId = 1,
            productIds = listOf(1, 2),
            idempotencyKey = orderId
        )

        val checkoutResult = checkoutUsecase.checkout(command = checkoutCommand).block()

        val paymentConfirmCommand = PaymentConfirmCommand(
            paymentKey = paymentKey,
            orderId = orderId,
            amount = checkoutResult!!.amount
        )

        paymentStatusUpdatePort.updatePaymentStatusToExecuting(
            paymentConfirmCommand.orderId,
            paymentConfirmCommand.paymentKey
        ).block()

        val paymentStatusUpdateCommand = PaymentStatusUpdateCommand(
            orderId = paymentConfirmCommand.orderId,
            paymentKey = paymentConfirmCommand.paymentKey,
            status = UNKNOWN,
            paymentFailure = PaymentFailure("UNKNOWN", "UNKNOWN")
        )

        paymentStatusUpdatePort.updatePaymentStatus(paymentStatusUpdateCommand).block()

        return paymentConfirmCommand
    }
}
