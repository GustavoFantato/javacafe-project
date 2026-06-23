package exception;

// Main class for outofstockexception related behavior
public class OutOfStockException extends Exception{
    // Handles OutOfStockException logic
    public OutOfStockException(String message){
        super(message);
    }
}