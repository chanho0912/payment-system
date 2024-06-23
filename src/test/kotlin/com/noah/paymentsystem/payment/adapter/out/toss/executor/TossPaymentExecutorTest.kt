package com.noah.paymentsystem.payment.adapter.out.toss.executor

import com.noah.paymentsystem.payment.adapter.out.web.toss.exception.PSPConfirmationException
import com.noah.paymentsystem.payment.adapter.out.web.toss.exception.TossPaymentError
import com.noah.paymentsystem.payment.adapter.out.web.toss.excutor.TossPaymentExecutor
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmCommand
import com.noah.paymentsystem.payment.test.PSPTestWebClientConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.UUID

@SpringBootTest
@Import(PSPTestWebClientConfiguration::class)
@Tag("TooLongTime")
class TossPaymentExecutorTest(
    @Autowired
    private val tossWebClientConfiguration: PSPTestWebClientConfiguration
) {

    @Test
    fun `should handle correctly various TossPaymentError scenarios`() {
        generateErrorScenarios().forEach { errorScenario ->
            val command = PaymentConfirmCommand(
                orderId = UUID.randomUUID().toString(),
                paymentKey = UUID.randomUUID().toString(),
                amount = 10000L
            )

            val paymentExecutor = TossPaymentExecutor(
                tossWebClientConfiguration.createTestTossWebClient(
                    "TossPayments-Test-Code" to errorScenario.errorCode
                ),
                uri = "/v1/payments/key-in"
            )

            try {
                paymentExecutor.execute(command).block()
            } catch (e: PSPConfirmationException) {
                assertThat(e.isSuccess).isEqualTo(errorScenario.isSuccess)
                assertThat(e.isFailure).isEqualTo(errorScenario.isFailure)
                assertThat(e.isUnknown).isEqualTo(errorScenario.isUnknown)
            }
        }
    }


    private fun generateErrorScenarios(): List<ErrorScenario> {
        return TossPaymentError.entries.map { error ->
            ErrorScenario(
                errorCode = error.name,
                isFailure = error.isFailure(),
                isUnknown = error.isUnknown(),
                isSuccess = error.isSuccess()
            )

        }
    }
}

data class ErrorScenario(
    val errorCode: String,
    val isFailure: Boolean,
    val isUnknown: Boolean,
    val isSuccess: Boolean
)
