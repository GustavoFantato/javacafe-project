package exception;

// Main class for invalidpaymentexception related behavior
public class InvalidPaymentException extends Exception{
    // Handles InvalidPaymentException logic
    public InvalidPaymentException(String message){
        super(message);
    }
}