package model;

public class SalesReport {

    public record TopItem(String name, int quantity, double revenue) {}

    private double totalRevenue;
    private int transactionCount;
    private TopItem[] top3;
    private java.util.Map<String, Integer> paymentBreakdown;
    private java.util.Map<String, Double> revenueSeries;

    public SalesReport() {
        this.top3 = new TopItem[3];
        this.paymentBreakdown = new java.util.HashMap<>();
        this.revenueSeries = new java.util.LinkedHashMap<>();
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public TopItem[] getTop3() {
        return top3;
    }

    public void setTop3(TopItem[] top3) {
        this.top3 = top3;
    }

    public java.util.Map<String, Integer> getPaymentBreakdown() {
        return paymentBreakdown;
    }

    public void setPaymentBreakdown(java.util.Map<String, Integer> paymentBreakdown) {
        this.paymentBreakdown = paymentBreakdown;
    }

    public java.util.Map<String, Double> getRevenueSeries() {
        return revenueSeries;
    }

    public void setRevenueSeries(java.util.Map<String, Double> revenueSeries) {
        this.revenueSeries = revenueSeries;
    }

    public TopItem getBestSeller() {
        return top3.length > 0 ? top3[0] : null;
    }
}
