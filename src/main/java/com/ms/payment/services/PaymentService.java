package com.ms.payment.services;

import com.ms.payment.domain.Payment;
import com.ms.payment.domain.PaymentEvent;
import com.ms.payment.domain.PaymentState;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {

    Payment newPayment(Payment payment);

    Payment newPreAuthPayment(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authPayment(Long paymentId);

}
