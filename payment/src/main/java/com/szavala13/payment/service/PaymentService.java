package com.szavala13.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.szavala13.payment.exceptions.PaymentNotFoundException;
import com.szavala13.payment.model.Payment;
import com.szavala13.payment.model.dto.PaymentMessage;
import com.szavala13.payment.model.enumeration.PaymentStatus;
import com.szavala13.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RestClient restClient;
    @Value("${service.customer}")
    private String baseUrl;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional
    public Payment createPayment(Payment payment) {
        payment.setPaymentStatus(PaymentStatus.PENDING);
        Payment newPayment = paymentRepository.save(payment);

        putBalance(payment.getAmount(), payment.getPayer());

        return newPayment;
    }

    public Optional<Payment> getPayment(Long id) {
        return paymentRepository.findById(id);
    }

    public Payment updateStatus(Long id, PaymentStatus newStatus) throws JsonProcessingException {
        if (newStatus == PaymentStatus.PENDING) {
            throw new IllegalArgumentException("No se puede cambiar el estado de un pago pendiente");
        }
        Payment payment = getPayment(id).orElseThrow(() -> new PaymentNotFoundException("Pago no encontrado"));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("El pago ya ha sido actualizado");
        }

        payment.setPaymentStatus(newStatus);
        Payment newPayment = paymentRepository.save(payment);

        if (newStatus == PaymentStatus.COMPLETED) {
            kafkaTemplate.send("payment_completed_notifications", getString(payment.getPayee(), payment.getAmount()));
        } else if (newStatus == PaymentStatus.CANCELED) {
            kafkaTemplate.send("payment_canceled_notifications", getString(payment.getPayer(), payment.getAmount()));
        }

        return newPayment;
    }

    private static String getString(Long customerId, BigDecimal amount) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        PaymentMessage
            paymentMessage = PaymentMessage.builder().customerId(customerId).amount(amount).build();
        return mapper.writeValueAsString(paymentMessage);
    }

    private void putBalance(BigDecimal amount, Long customerId) {
        final URI url = UriComponentsBuilder
            .fromHttpUrl(baseUrl)
            .path("/api/balance/{customerId}/subtract")
            .queryParam("amount", amount)
            .buildAndExpand(customerId)
            .toUri();
        restClient.put()
            .uri(url)
            .retrieve()
            .body(String.class);
    }
}
