package model;

// Main class for salesreport related behavior
public class SalesReport {

    public record TopItem(String name, int quantity, double revenue) {}

    private double totalRevenue;
    private int transactionCount;
    private TopItem[] top3;
    private java.util.Map<String, Integer> paymentBreakdown;
    private java.util.Map<String, Double> revenueSeries;

    // Handles SalesReport logic
    public SalesReport() {
        this.top3 = new TopItem[3];
        this.paymentBreakdown = new java.util.HashMap<>();
        this.revenueSeries = new java.util.LinkedHashMap<>();
    }

    // Handles getTotalRevenue logic
    public double getTotalRevenue() {
        return totalRevenue;
    }

    // Handles setTotalRevenue logic
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    // Handles getTransactionCount logic
    public int getTransactionCount() {
        return transactionCount;
    }

    // Handles setTransactionCount logic
    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    // Handles getTop3 logic
    public TopItem[] getTop3() {
        return top3;
    }

    // Handles setTop3 logic
    public void setTop3(TopItem[] top3) {
        this.top3 = top3;
    }

    // Handles getPaymentBreakdown logic
    public java.util.Map<String, Integer> getPaymentBreakdown() {
        return paymentBreakdown;
    }

    // Handles setPaymentBreakdown logic
    public void setPaymentBreakdown(java.util.Map<String, Integer> paymentBreakdown) {
        this.paymentBreakdown = paymentBreakdown;
    }

    // Handles getRevenueSeries logic
    public java.util.Map<String, Double> getRevenueSeries() {
        return revenueSeries;
    }

    // Handles setRevenueSeries logic
    public void setRevenueSeries(java.util.Map<String, Double> revenueSeries) {
        this.revenueSeries = revenueSeries;
    }

    // Handles getBestSeller logic
    public TopItem getBestSeller() {
        return top3.length > 0 ? top3[0] : null;
    }
}