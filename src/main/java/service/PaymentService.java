package service;

import exception.InvalidPaymentException;
import model.Order;
import service.enums.PaymentMethods;

public interface PaymentService {

    void validate(Order order, double cashReceived) throws InvalidPaymentException;

    static PaymentService forMethod(PaymentMethods method) {
        return switch (method) {
            case CARD -> new CardPaymentService();
            case PIX -> new PixPaymentService();
            case CASH -> new CashPaymentService();
        };
    }
}

class CardPaymentService implements PaymentService {

    @Override
    // Handles validate logic
    public void validate(Order order, double cashReceived) {
        System.out.println("[Checkout] CARD SELECTED - Validation success");
    }
}

class PixPaymentService implements PaymentService {

    @Override
    // Handles validate logic
    public void validate(Order order, double cashReceived) {
        System.out.println("[Checkout] PIX SELECTED - Validation success");
    }
}

class CashPaymentService implements PaymentService {

    @Override
    public void validate(Order order, double cashReceived) throws InvalidPaymentException {
        double cartCost = order.getTotalCost();
        System.out.println("[Checkout] CASH SELECTED");

        if (cashReceived < cartCost) {
            throw new InvalidPaymentException("Valor recebido insuficiente para finalizar o pedido.");
        }

        System.out.printf("CART TOTAL (WITH TAXES): R$%.2f%n", cartCost);
        System.out.printf("CASH RECEIVED: R$%.2f%n", cashReceived);
        System.out.printf("CHANGE: R$%.2f%n", cashReceived - cartCost);
    }
}