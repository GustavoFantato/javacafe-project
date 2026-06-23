package model;

import model.enums.Status;
import java.util.ArrayList; 
import java.util.List;

/**
 * Represents a customer's order containing multiple order items and a specific status.
 */
public class Order {

    private List<OrderItem> items;
    private int orderID;
    private static int idCount = 0;
    private Status status;
    private String customerName;

    public Order() {
        this.orderID = 0;
        this.items = new ArrayList<>(); 
        this.status = Status.PENDING;
    }

    /**
     * Adds an item to the order. If the product already exists in the cart, 
     * it aggregates the quantity instead of creating a new entry.
     */
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

    /**
     * @return Sum of all items inside the order (without tax).
     */
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
    public double getTaxCost() {
        return getListCost() * 0.10;
    }

    /**
     * @return Final order cost including taxes.
     */
    public double getTotalCost() {
        return getListCost() + getTaxCost();
    }

    public int getOrderID() {
        return orderID > 0 ? orderID : peekNextOrderId();
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

    public void ensureOrderId() {
        if (orderID <= 0) {
            orderID = ++idCount;
        }
    }

    public static int peekNextOrderId() {
        return idCount + 1;
    }

    public static void syncCounter(int lastUsedId) {
        idCount = Math.max(idCount, lastUsedId);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US, "Order #%d [Status: %s] - Items: %d - Subtotal: R$ %.2f - Total: R$ %.2f",
                orderID, status, items.size(), getListCost(), getTotalCost());
    }
}