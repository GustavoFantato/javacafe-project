package model;

import model.enums.Status;
import java.util.ArrayList; // To use the order's item list
import java.util.List;

public class Order {

    // Attributes
    private List<OrderItem> items;
    private final int orderID;
    private static int idCount = 0;
    private Status status;
    private String customerName;

    // Constructor
    public Order() {
        this.orderID = ++idCount;
        this.items = new ArrayList<>(); // Creates order's item list
        this.status = Status.PENDING;
    }

    // Methods

    // List manipulation
    public void addItem(OrderItem newItem) {
        for (OrderItem existingItem : items) {
            if (existingItem.getProduct().getID() == newItem.getProduct().getID()) {
                existingItem.setQtd(existingItem.getQtd() + newItem.getQtd());
                return;
            }
        }
        this.items.add(newItem);
    }

    public void removeItem(OrderItem item) {
        this.items.remove(item);
    }

    // Cost calculation (without tax)
    public double getListCost() {
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public double getTaxCost() {
        return getListCost() * 0.10;
    }

    // Calculates total cost WITH the tax
    public double getTotalCost() {
        return getListCost() + getTaxCost();
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

    public String getCustomerName() {
        return customerName;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US, "Order #%d [Status: %s] - Items: %d - Subtotal: R$ %.2f - Total: R$ %.2f",
                orderID, status, items.size(), getListCost(), getTotalCost());
    }
}