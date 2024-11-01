package com.szavala13.customer.controller;

import com.szavala13.customer.exceptions.CustomerNotFoundException;
import com.szavala13.customer.exceptions.InsufficientBalanceException;
import com.szavala13.customer.service.BalanceService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @PutMapping("/{customerId}/subtract")
    public ResponseEntity<String> subtractBalance(@PathVariable Long customerId, @RequestParam BigDecimal amount) {
        try {
            balanceService.subtractBalance(customerId, amount);
            return ResponseEntity.ok("Saldo actualizado con éxito");
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InsufficientBalanceException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
