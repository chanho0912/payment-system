package com.noah.paymentsystem.payment.application.domain

data class PaymentFailure (
  val errorCode: String,
  val message: String
)
