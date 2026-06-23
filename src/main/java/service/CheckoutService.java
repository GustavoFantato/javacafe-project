package service;

import exception.InvalidPaymentException;
import model.Order;
import model.OrderItem;
import model.Product;
import service.enums.PaymentMethods;
import java.io.*;
import java.time.LocalDateTime;

public class CheckoutService {

    // Attributes
    private InventoryService inventoryService;
    private String filePath;
    private static int transactionId = 0;

    // Constructor
    public CheckoutService(InventoryService inventoryService, String filePath) {
        this.inventoryService = inventoryService;
        this.filePath = filePath;
        loadLastTransactionState(); // Gets the last transaction and order IDs from the sales csv
    }

    // Methods

    // Process the payment method to its process
    public boolean processPayment(PaymentMethods paymentMethod, Order currentOrder, double cashReceived) throws InvalidPaymentException {
        PaymentService paymentService = PaymentService.forMethod(paymentMethod);
        paymentService.validate(currentOrder, cashReceived);
        return true;
    }

    // Process, logs and saves the final sale into the CSV and decreases stock
    public void finishSale(Order currentOrder, PaymentMethods paymentMethod, double cashReceived) {
        currentOrder.ensureOrderId();
        transactionId++; // Increments the transaction ID that will be logged

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy~HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);

        double cartCost = currentOrder.getTotalCost();
        int orderId = currentOrder.getOrderID();

        double change = 0.0;
        if (paymentMethod == PaymentMethods.CASH) {
            change = calculateChange(cartCost, cashReceived);
        }

        try {
            for (OrderItem item : currentOrder.getItems()) {
                Product p = item.getProduct();
                inventoryService.decreaseProductStock(p.getID(), item.getQtd());
            }

            File salesFile = new File(this.filePath);
            boolean writeHeader = !salesFile.exists() || salesFile.length() == 0;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath, true))) {

                if (writeHeader) {
                    writer.write("transactionId,orderId,productId,productName,qtd,unitPrice,subtotal");
                    writer.newLine();
                }

                for (OrderItem item : currentOrder.getItems()) {
                    Product p = item.getProduct();

                    String line = String.format(java.util.Locale.US, "%d,%d,%d,%s,%d,%.2f,%.2f",
                            transactionId,              // saleId
                            orderId,                    // orderId
                            p.getID(),                  // productId
                            p.getName(),                // productName
                            item.getQtd(),              // quantity
                            p.getPrice(),               // unitPrice
                            item.getSubtotal()          // subtotal price
                    );
                    writer.write(line);
                    writer.newLine();
                }

                String changeLine = String.format(java.util.Locale.US, "%c,%d,%s,%d,%s,%.2f,%.2f",
                        'f',                      // footer identifier
                        transactionId,                  // saleId
                        timestamp,                      // timestamp
                        orderId,                        // orderId
                        paymentMethod.name(),           // paymentMethod
                        cartCost,                       // totalPrice with tax
                        change                          // change
                );
                writer.write(changeLine);
                writer.newLine();
            }

            // Sets the STATUS as PAID
            currentOrder.setStatus(model.enums.Status.PAID);
            System.out.println("[Checkout] " + currentOrder.toString());

        } catch (Exception e) {
            System.err.println("ERROR during finishSale: " + e.getMessage());
        }
    }

    private void loadLastTransactionState() {
        File file = new File(this.filePath);
        if (!file.exists() || file.length() == 0) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lastTransactionId = 0;
            int lastOrderId = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("transactionId") || line.startsWith("footer")) {
                    continue;
                }

                String[] tokens = line.split(",");
                if (tokens[0].equals("f")) {
                    lastTransactionId = Math.max(lastTransactionId, Integer.parseInt(tokens[1]));
                    lastOrderId = Math.max(lastOrderId, Integer.parseInt(tokens[3]));
                } else if (tokens.length >= 2) {
                    lastTransactionId = Math.max(lastTransactionId, Integer.parseInt(tokens[0]));
                    lastOrderId = Math.max(lastOrderId, Integer.parseInt(tokens[1]));
                }
            }

            transactionId = lastTransactionId;
            Order.syncCounter(lastOrderId);
        } catch (Exception e) {
            System.err.println("[Checkout] Could not load last transaction ID. Error: " + e.getMessage());
        }
    }

    public double calculateChange(double cartCost, double cashReceived) {
        return (cashReceived - cartCost);
    }
}