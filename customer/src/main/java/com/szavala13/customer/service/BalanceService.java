package com.szavala13.customer.service;

import com.szavala13.customer.exceptions.CustomerNotFoundException;
import com.szavala13.customer.exceptions.InsufficientBalanceException;
import com.szavala13.customer.model.Customer;
import com.szavala13.customer.repository.CustomerRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {
    private final CustomerRepository customerRepository;

    public void updateBalance(Long payeeId, BigDecimal amount) {
        Customer customer = customerRepository.findById(payeeId).orElseThrow(() -> new CustomerNotFoundException
            ("Cliente no encontrado"));

        customer.setBalance(customer.getBalance().add(amount));
        log.info("Balance actualizado: {}", customer.getBalance());
        customerRepository.save(customer);
    }

    public void subtractBalance(Long payeeId, BigDecimal amount) {
        Customer customer = customerRepository.findById(payeeId).orElseThrow(() -> new CustomerNotFoundException
            ("Cliente no encontrado"));

        if (customer.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }

        customer.setBalance(customer.getBalance().subtract(amount));
        log.info("Nuevo saldo : {}", customer.getBalance());
        customerRepository.save(customer);
    }
}
