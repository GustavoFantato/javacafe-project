package model;

import model.enums.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer's order containing multiple order items and a specific status.
 */
// Main class for order related behavior
public class Order {

    private List<OrderItem> items;
    private int orderID;
    private static int idCount = 0;
    private Status status;
    private String customerName;

    // Handles Order logic
    public Order() {
        this.orderID = 0;
        this.items = new ArrayList<>();
        this.status = Status.PENDING;
    }

    /**
     * Adds an item to the order. If the product already exists in the cart,
     * it aggregates the quantity instead of creating a new entry.
     */
    // Handles addItem logic
    public void addItem(OrderItem newItem) {
        for (OrderItem existingItem : items) {
            if (existingItem.getProduct().getID() == newItem.getProduct().getID()) {
                existingItem.setQtd(existingItem.getQtd() + newItem.getQtd());
                return;
            }
        }
        this.items.add(newItem);
    }

    // Handles removeItem logic
    public void removeItem(OrderItem item) {
        this.items.remove(item);
    }

    /**
     * @return Sum of all items inside the order (without tax).
     */
    // Handles getListCost logic
    public double getListCost() {
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    /**
     * @return Fixed 10% tax cost based on the list cost.
     */
    // Handles getTaxCost logic
    public double getTaxCost() {
        return getListCost() * 0.10;
    }

    /**
     * @return Final order cost including taxes.
     */
    // Handles getTotalCost logic
    public double getTotalCost() {
        return getListCost() + getTaxCost();
    }

    // Handles getOrderID logic
    public int getOrderID() {
        return orderID > 0 ? orderID : peekNextOrderId();
    }

    // Handles getStatus logic
    public Status getStatus() {
        return status;
    }

    // Handles getItems logic
    public List<OrderItem> getItems() {
        return items;
    }

    // Handles getCustomerName logic
    public String getCustomerName() {
        return customerName;
    }

    // Handles setStatus logic
    public void setStatus(Status status) {
        this.status = status;
    }

    // Handles setCustomerName logic
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // Handles ensureOrderId logic
    public void ensureOrderId() {
        if (orderID <= 0) {
            orderID = ++idCount;
        }
    }

    // Handles peekNextOrderId logic
    public static int peekNextOrderId() {
        return idCount + 1;
    }

    // Handles syncCounter logic
    public static void syncCounter(int lastUsedId) {
        idCount = Math.max(idCount, lastUsedId);
    }

    @Override
    // Handles toString logic
    public String toString() {
        return String.format(java.util.Locale.US, "Order #%d [Status: %s] - Items: %d - Subtotal: R$ %.2f - Total: R$ %.2f",
                orderID, status, items.size(), getListCost(), getTotalCost());
    }
}