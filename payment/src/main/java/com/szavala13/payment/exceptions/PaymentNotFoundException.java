package com.szavala13.payment.exceptions;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException() {
        super();
    }

    public PaymentNotFoundException(String message) {
        super(message);
    }

    public PaymentNotFoundException(long id) {
        super("Payment not found: " + id);
    }
}
