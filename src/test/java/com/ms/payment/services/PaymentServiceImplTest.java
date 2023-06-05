package com.ms.payment.services;

import com.ms.payment.domain.Payment;
import com.ms.payment.domain.PaymentEvent;
import com.ms.payment.domain.PaymentState;
import com.ms.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("4.55")).build();
    }

    @Test
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);
        assertEquals(savedPayment.getPaymentState(), PaymentState.NEW);
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
        Optional<Payment> preAuthPayment = paymentRepository.findById(savedPayment.getId());
        assertTrue(preAuthPayment.isPresent());
        assertTrue(Arrays.asList(PaymentState.PRE_AUTH, PaymentState.PRE_AUTH_ERROR).contains(preAuthPayment.get().getPaymentState()));
        System.out.println(sm.getState().getId());
        System.out.println(preAuthPayment.get());
    }

    @RepeatedTest(10)
    void auth() {
        Payment savedPayment = paymentService.newPayment(payment);
        assertEquals(savedPayment.getPaymentState(), PaymentState.NEW);
        StateMachine<PaymentState, PaymentEvent> preAuthSm = paymentService.preAuth(savedPayment.getId());
        if (preAuthSm.getState().getId().equals(PaymentState.PRE_AUTH)) {
            System.out.println("Payment pre authorized");
            StateMachine<PaymentState, PaymentEvent> authSm = paymentService.authPayment(savedPayment.getId());
            // assertTrue(Arrays.asList(PaymentState.AUTH, PaymentState.AUTH_ERROR).contains(authSm.getState().getId()));
            System.out.println("Result of auth : " + authSm.getState().getId());
        } else {
            System.out.println("Payment can not pre authorized");

        }
    }

    @Test
    void justAuth() {
        Payment savedPayment = paymentService.newPreAuthPayment(payment);
        assertEquals(savedPayment.getPaymentState(), PaymentState.PRE_AUTH);
        StateMachine<PaymentState, PaymentEvent> authSm = paymentService.authPayment(savedPayment.getId());
        // assertTrue(Arrays.asList(PaymentState.AUTH, PaymentState.AUTH_ERROR).contains(authSm.getState().getId()));
        System.out.println("Result of auth : " + authSm.getState().getId());
    }
}
