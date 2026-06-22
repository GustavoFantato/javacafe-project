package service;

import model.SalesReport;
import model.SalesReport.TopItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportsService {

    private static final DateTimeFormatter FOOTER_DATE =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final String filePath;

    public ReportsService(String filePath) {
        this.filePath = filePath;
    }

    public SalesReport generateReport(LocalDate start, LocalDate end) {
        SalesReport report = new SalesReport();
        report.setTop3(new TopItem[3]);

        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return report;
        }

        Map<Integer, List<String[]>> transactionItems = new HashMap<>();
        double totalRevenue = 0;
        int transactionCount = 0;
        Map<String, Integer> itemQuantities = new HashMap<>();
        Map<String, Double> itemRevenues = new HashMap<>();
        Map<String, Integer> paymentBreakdown = new HashMap<>();
        Map<String, Double> dailyRevenue = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("transactionId") || line.startsWith("footer")) {
                    continue;
                }

                String[] tokens = line.split(",");
                if (tokens[0].equals("f")) {
                    if (tokens.length < 6) {
                        continue;
                    }

                    int transactionId = Integer.parseInt(tokens[1]);
                    LocalDate saleDate = parseFooterDate(tokens[2]);
                    if (saleDate == null || saleDate.isBefore(start) || saleDate.isAfter(end)) {
                        transactionItems.remove(transactionId);
                        continue;
                    }

                    transactionCount++;
                    double cartCost = Double.parseDouble(tokens[5]);
                    totalRevenue += cartCost;

                    String dayKey = saleDate.format(DateTimeFormatter.ofPattern("dd/MM"));
                    dailyRevenue.merge(dayKey, cartCost, Double::sum);

                    String payment = tokens[4];
                    paymentBreakdown.merge(payment, 1, Integer::sum);

                    List<String[]> items = transactionItems.remove(transactionId);
                    if (items != null) {
                        for (String[] itemTokens : items) {
                            String productName = itemTokens[3];
                            int qtd = Integer.parseInt(itemTokens[4]);
                            double subtotal = Double.parseDouble(itemTokens[6]);
                            itemQuantities.merge(productName, qtd, Integer::sum);
                            itemRevenues.merge(productName, subtotal, Double::sum);
                        }
                    }
                } else if (tokens.length >= 7) {
                    int transactionId = Integer.parseInt(tokens[0]);
                    transactionItems.computeIfAbsent(transactionId, k -> new ArrayList<>()).add(tokens);
                }
            }
        } catch (Exception e) {
            System.err.println("[Reports] Erro ao ler vendas: " + e.getMessage());
        }

        report.setTotalRevenue(totalRevenue);
        report.setTransactionCount(transactionCount);
        report.setPaymentBreakdown(paymentBreakdown);
        report.setDailyRevenue(fillDailyRevenueRange(dailyRevenue, start, end));
        report.setTop3(buildTop3(itemQuantities, itemRevenues));

        return report;
    }

    private TopItem[] buildTop3(Map<String, Integer> itemQuantities, Map<String, Double> itemRevenues) {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(itemQuantities.entrySet());
        sorted.sort(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed());

        TopItem[] top3 = new TopItem[3];
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            String name = sorted.get(i).getKey();
            int qty = sorted.get(i).getValue();
            double revenue = itemRevenues.getOrDefault(name, 0.0);
            top3[i] = new TopItem(name, qty, revenue);
        }
        return top3;
    }

    private Map<String, Double> fillDailyRevenueRange(Map<String, Double> dailyRevenue,
                                                      LocalDate start, LocalDate end) {
        Map<String, Double> filled = new LinkedHashMap<>();
        LocalDate cursor = start;
        DateTimeFormatter keyFmt = DateTimeFormatter.ofPattern("dd/MM");

        while (!cursor.isAfter(end)) {
            String key = cursor.format(keyFmt);
            filled.put(key, dailyRevenue.getOrDefault(key, 0.0));
            cursor = cursor.plusDays(1);
        }

        return filled;
    }

    private LocalDate parseFooterDate(String timestampField) {
        try {
            String datePart = timestampField.split("~")[0];
            return LocalDate.parse(datePart, FOOTER_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    public String formatCurrency(double value) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", value);
    }
}
