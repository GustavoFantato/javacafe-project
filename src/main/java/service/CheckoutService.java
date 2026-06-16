package service;

import exception.InvalidPaymentException;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import service.enums.PaymentMethods;

import java.util.Random;

public class CheckoutService{

    // Attributes
    private InventoryService inventoryService;
    private String filePath;
    private static int transactionId = 0;

    // Constructor
    public CheckoutService(InventoryService inventoryService, String filePath){
        this.inventoryService = inventoryService;
    }

    // Methods

    // Process the payment method to its process
    public void processPayment(PaymentMethods paymentMethod, double cartCost, double cashReceived) throws InvalidPaymentException {

        Random random = new Random();

        double change;
        float delay = 1 + random.nextFloat() * (7 - 1); // Delay: 1;0 ~ 7.0 seconds

        switch(paymentMethod){
            case CARD:
                System.out.println("[Checkout] CARD SELECTED");
                System.out.println("[Checkout] Processing payment...");

                executeWithDelay(delay, ()->{
                    System.out.println("[Checkout] Payment Approved!");
                    finishSale();
                } );
                break;
            case PIX:
                System.out.println("[Checkout] PIX SELECTED");
                System.out.println("[Checkout] Processing payment...");

                executeWithDelay(delay, ()->{
                    System.out.println("[Checkout] Payment Approved!");
                    finishSale();
                });
                break;
            case CASH:
                System.out.println("[Checkout] CASH SELECTED");
                System.out.println("[Checkout] Processing payment...");

                if(cashReceived < cartCost){ // Verify if it is an enough value
                    throw new InvalidPaymentException("ERROR: No enough cash to finish the order");
                }

                change = processChange(cartCost, cashReceived);
                System.out.println("[Checkout] Payment Approved!");
                finishSale();
        }
    }

    // Prints, calculates and returns the change
    private double processChange(double cartCost, double cashReceived){

        System.out.printf("CART: R$%.2f", cartCost);
        System.out.printf("CASH RECEIVED: R$%.2f", cashReceived);
        System.out.printf("CHANGE: R$%.2f", cashReceived - cartCost);

        return (cashReceived - cartCost);
    }

    // Writes the data in the sales file
    private void finishSale(){



    }


    // Sales file manipulation
    private void logNewSale(){}
    private void logNewChange(){}

    // Auxiliary method to execute the processPayment method with delay
    private void executeWithDelay(double seconds, Runnable action) {
        PauseTransition pause = new PauseTransition(Duration.seconds(seconds));

        pause.setOnFinished(event -> {
            // Executes the code block (param)
            action.run();
        });
        pause.play(); // Clock starts running
    }
}