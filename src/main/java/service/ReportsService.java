package service;

import model.SalesReport;
import model.SalesReport.TopItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service responsible for parsing sales data and generating business intelligence reports.
 */
// Main class for reportsservice related behavior
public class ReportsService {

    private static final DateTimeFormatter FOOTER_DATE =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter FOOTER_TIMESTAMP =
            DateTimeFormatter.ofPattern("dd-MM-yyyy~HH:mm:ss");

    public enum RevenueGrouping {
        HOURLY,
        DAILY,
        WEEKLY
    }

    private final String filePath;

    // Handles ReportsService logic
    public ReportsService(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads the sales CSV file and aggregates data for the specified period and grouping.
     * * @param start The start date of the reporting period.
     * @param end The end date of the reporting period.
     * @param grouping The granularity of the revenue data (Hourly, Daily, Weekly).
     * @return A SalesReport object containing the aggregated metrics.
     */
    // Handles generateReport logic
    public SalesReport generateReport(LocalDate start, LocalDate end, RevenueGrouping grouping) {
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
        Map<String, Double> revenueSeries = new HashMap<>();

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
                    LocalDateTime saleTimestamp = parseFooterTimestamp(tokens[2]);
                    if (saleTimestamp == null) {
                        transactionItems.remove(transactionId);
                        continue;
                    }

                    LocalDate saleDate = saleTimestamp.toLocalDate();
                    if (saleDate.isBefore(start) || saleDate.isAfter(end)) {
                        transactionItems.remove(transactionId);
                        continue;
                    }

                    transactionCount++;
                    double cartCost = Double.parseDouble(tokens[5]);
                    totalRevenue += cartCost;

                    String periodKey = formatRevenueKey(saleTimestamp, grouping);
                    revenueSeries.merge(periodKey, cartCost, Double::sum);

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
            System.err.println("[Reports] Error reading sales: " + e.getMessage());
        }

        report.setTotalRevenue(totalRevenue);
        report.setTransactionCount(transactionCount);
        report.setPaymentBreakdown(paymentBreakdown);
        report.setRevenueSeries(fillRevenueSeriesRange(revenueSeries, start, end, grouping));
        report.setTop3(buildTop3(itemQuantities, itemRevenues));

        return report;
    }

    // Handles buildTop3 logic
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

    private Map<String, Double> fillRevenueSeriesRange(Map<String, Double> revenueSeries,
                                                       LocalDate start, LocalDate end,
                                                       RevenueGrouping grouping) {
        Map<String, Double> filled = new LinkedHashMap<>();
        if (grouping == RevenueGrouping.HOURLY) {
            int minHour = 8;
            int maxHour = 20;
            if (!revenueSeries.isEmpty()) {
                minHour = revenueSeries.keySet().stream()
                        .mapToInt(k -> Integer.parseInt(k.replace("h", "")))
                        .min().orElse(8);
                maxHour = revenueSeries.keySet().stream()
                        .mapToInt(k -> Integer.parseInt(k.replace("h", "")))
                        .max().orElse(20);
                minHour = Math.max(0, minHour - 1);
                maxHour = Math.min(23, maxHour + 1);
            }
            for (int hour = minHour; hour <= maxHour; hour++) {
                String key = String.format("%02dh", hour);
                filled.put(key, revenueSeries.getOrDefault(key, 0.0));
            }
            return filled;
        }

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            String key = grouping == RevenueGrouping.WEEKLY
                    ? formatWeekdayKey(cursor)
                    : cursor.format(DateTimeFormatter.ofPattern("dd/MM"));
            filled.put(key, revenueSeries.getOrDefault(key, 0.0));
            cursor = cursor.plusDays(1);
        }

        return filled;
    }

    // Handles formatRevenueKey logic
    private String formatRevenueKey(LocalDateTime timestamp, RevenueGrouping grouping) {
        return switch (grouping) {
            case HOURLY -> String.format("%02dh", timestamp.getHour());
            case WEEKLY -> formatWeekdayKey(timestamp.toLocalDate());
            case DAILY -> timestamp.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM"));
        };
    }

    // Handles formatWeekdayKey logic
    private String formatWeekdayKey(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Seg";
            case TUESDAY -> "Ter";
            case WEDNESDAY -> "Qua";
            case THURSDAY -> "Qui";
            case FRIDAY -> "Sex";
            case SATURDAY -> "Sab";
            case SUNDAY -> "Dom";
        };
    }

    // Handles parseFooterTimestamp logic
    private LocalDateTime parseFooterTimestamp(String timestampField) {
        try {
            return LocalDateTime.parse(timestampField, FOOTER_TIMESTAMP);
        } catch (Exception e) {
            return null;
        }
    }

    // Handles formatCurrency logic
    public String formatCurrency(double value) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", value);
    }
}