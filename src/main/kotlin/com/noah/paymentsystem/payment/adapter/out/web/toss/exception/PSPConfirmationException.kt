package com.noah.paymentsystem.payment.adapter.out.web.toss.exception

data class PSPConfirmationException(
    val errorCode: String,
    val errorMessage: String,
    val isSuccess: Boolean,
    val isFailure: Boolean,
    val isUnknown: Boolean,
    val isRetryableError: Boolean,
    override val cause: Throwable? = null
) : RuntimeException(errorMessage, cause)
