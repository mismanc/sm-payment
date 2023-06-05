package com.ms.payment.services;

import com.ms.payment.domain.Payment;
import com.ms.payment.domain.PaymentEvent;
import com.ms.payment.domain.PaymentState;
import com.ms.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor stateChangeInterceptor;

    @Override
    public Payment newPayment(Payment payment) {
        payment.setPaymentState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment newPreAuthPayment(Payment payment) {
        payment.setPaymentState(PaymentState.PRE_AUTH);
        return paymentRepository.save(payment);
    }


    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = buildWithId(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
        return sm;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authPayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = buildWithId(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE);
        return sm;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent paymentEvent) {
        Message message = MessageBuilder.withPayload(paymentEvent).setHeader(PAYMENT_ID_HEADER, paymentId).build();
        sm.sendEvent(message);
    }

    private StateMachine<PaymentState, PaymentEvent> buildWithId(Long paymentId) {
        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
        if (paymentOptional.isPresent()) {
            StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(paymentOptional.get().getId().toString());
            sm.stop();
            sm.getStateMachineAccessor().doWithAllRegions(sma -> {
                sma.addStateMachineInterceptor(stateChangeInterceptor);
                sma.resetStateMachine(new DefaultStateMachineContext<>(paymentOptional.get().getPaymentState(), null, null, null));
            });
            sm.start();
            return sm;
        }
        return null;
    }
}
