package com.szavala13.customer.service;

import com.szavala13.customer.model.dto.PaymentMessage;
import com.szavala13.customer.utils.JsonUtils;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCanceledConsumer {

    private final BalanceService balanceService;

    @KafkaListener(topics = "payment_canceled_notifications", groupId = "payment-canceled-group")
    public void handleCanceledPayment(String message) {
        log.info("payment_canceled_notifications");
        PaymentMessage paymentMessage = JsonUtils.parseJson(message, PaymentMessage.class);
        if (Objects.nonNull(paymentMessage)) {
            try {
                balanceService.updateBalance(paymentMessage.getCustomerId(), paymentMessage.getAmount());
            } catch (Exception e) {
                log.error("Error updating balance: {}", e.getMessage());
            }
        }
    }
}

