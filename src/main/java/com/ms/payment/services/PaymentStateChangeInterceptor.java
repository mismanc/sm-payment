package com.ms.payment.services;

import com.ms.payment.domain.Payment;
import com.ms.payment.domain.PaymentEvent;
import com.ms.payment.domain.PaymentState;
import com.ms.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;


@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message, Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine, StateMachine<PaymentState, PaymentEvent> rootStateMachine) {
        Optional.ofNullable(message).ifPresent(msg->{
            Long paymentId = (Long) msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L);
            Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
            paymentOptional.ifPresent(payment -> {
                payment.setPaymentState(state.getId());
                paymentRepository.save(payment);
            });
        });
    }
}
