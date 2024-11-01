package com.szavala13.customer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.szavala13.customer.model.dto.PaymentMessage;
import com.szavala13.customer.utils.JsonUtils;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentCompletedConsumer {

    private final BalanceService balanceService;

    @KafkaListener(topics = "payment_completed_notifications", groupId = "payment-completed-group")
    public void handleCompletedPayment(String message) {
        log.info("payment_completed_notifications");
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
