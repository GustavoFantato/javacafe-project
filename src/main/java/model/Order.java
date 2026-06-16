package model;

import model.enums.Status;
import java.util.ArrayList; // To use the order's item list
import java.util.List;

public class Order{

    // Attributes

    private List<OrderItem> items; // Dynamic list
    private final int orderID;
    private static int idCount = 0; //
    private Status status;

    // Constructor
    public Order(){
        this.orderID = ++idCount;
        this.items = new ArrayList<>(); // Creates order's item list
        this.status = Status.PENDING;
    }

    // Methods

    // List manipulation
    public void addItem(OrderItem item){ // Add a new item to the list
        this.items.add(item);
    }

    public void removeItem(OrderItem item){ // Remove an item from the list
        this.items.remove(item);
    }

    // Cost calculation
    public double getListCost(){ // returns the list value
        double total = 0.0;

        for (OrderItem item : items){
            total += item.getSubtotal();
        }

        return total;
    }

    // Getters

    public int getOrderID() {
        return orderID;
    }
    public Status getStatus() {
        return status;
    }
    public List<OrderItem> getItems() {
        return items;
    }

    // Setters
    public void setStatus(Status status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "Order #" + orderID + " [Status: " + status + "] - Total Items: " + items.size() + " - Total: R$ " + getListCost();
    }
}