package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.SalesReport;
import model.SalesReport.TopItem;
import service.ReportsService;
import service.enums.PaymentMethods;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Map;

// Main class for reportscontroller related behavior
public class ReportsController {

    @FXML private ToggleButton btnToday;
    @FXML private ToggleButton btnWeek;
    @FXML private ToggleButton btnMonth;
    @FXML private HBox customDateRow;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label reportRangeLabel;
    @FXML private Label kpiRevenue;
    @FXML private Label kpiRevenueDelta;
    @FXML private Label kpiTransactions;
    @FXML private Label kpiAvgTicket;
    @FXML private Label kpiBestItem;
    @FXML private Label kpiBestQty;
    @FXML private BarChart<String, Number> revenueChart;
    @FXML private LineChart<String, Number> revenueLineChart;
    @FXML private CategoryAxis revenueXAxis;
    @FXML private NumberAxis revenueYAxis;
    @FXML private Label top1Name;
    @FXML private Label top1Detail;
    @FXML private Label top2Name;
    @FXML private Label top2Detail;
    @FXML private Label top3Name;
    @FXML private Label top3Detail;
    @FXML private PieChart paymentPieChart;
    @FXML private Label lastUpdatedLabel;
    @FXML private ToggleButton chartBarsToggle;
    @FXML private ToggleButton chartLinesToggle;

    private ReportsService reportsService;
    private LocalDate currentStart;
    private LocalDate currentEnd;
    private ReportsService.RevenueGrouping currentGrouping = ReportsService.RevenueGrouping.DAILY;

    private final ToggleGroup periodGroup = new ToggleGroup();
    private final ToggleGroup chartTypeGroup = new ToggleGroup();
    private final DateTimeFormatter displayDateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    // Handles initialize logic
    public void initialize() {
        btnToday.setToggleGroup(periodGroup);
        btnWeek.setToggleGroup(periodGroup);
        btnMonth.setToggleGroup(periodGroup);
        periodGroup.selectToggle(btnToday);
        chartBarsToggle.setToggleGroup(chartTypeGroup);
        chartLinesToggle.setToggleGroup(chartTypeGroup);
        chartTypeGroup.selectToggle(chartBarsToggle);

        revenueChart.setLegendVisible(false);
        revenueLineChart.setLegendVisible(false);
        revenueChart.setCategoryGap(12);
        revenueChart.setBarGap(3);
        revenueYAxis.setForceZeroInRange(true);
        revenueYAxis.setMinorTickVisible(false);

        paymentPieChart.setLabelsVisible(false);
        paymentPieChart.setLegendVisible(true);
        paymentPieChart.setStartAngle(90);
        setChartType(false);
    }

    // Handles setReportsService logic
    public void setReportsService(ReportsService reportsService) {
        this.reportsService = reportsService;
        loadToday();
    }

    // Handles setSalesFilePath logic
    public void setSalesFilePath(String salesFilePath) {
        this.reportsService = new ReportsService(salesFilePath);
        loadToday();
    }

    @FXML
    // Handles refreshReport logic
    public void refreshReport() {
        if (currentStart != null && currentEnd != null) {
            processSalesData(currentStart, currentEnd);
        } else {
            loadToday();
        }
    }

    @FXML
    // Handles loadToday logic
    private void loadToday() {
        selectPeriod(btnToday);
        currentStart = LocalDate.now();
        currentEnd = LocalDate.now();
        currentGrouping = ReportsService.RevenueGrouping.HOURLY;
        reportRangeLabel.setText("Hoje — " + currentStart.format(displayDateFmt));
        processSalesData(currentStart, currentEnd);
    }

    @FXML
    // Handles loadWeek logic
    private void loadWeek() {
        selectPeriod(btnWeek);
        currentStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        currentEnd = currentStart.plusDays(6);
        currentGrouping = ReportsService.RevenueGrouping.WEEKLY;
        reportRangeLabel.setText(String.format("%s — %s",
                currentStart.format(displayDateFmt), currentEnd.format(displayDateFmt)));
        processSalesData(currentStart, currentEnd);
    }

    @FXML
    // Handles loadMonth logic
    private void loadMonth() {
        selectPeriod(btnMonth);
        currentStart = LocalDate.now().withDayOfMonth(1);
        currentEnd = LocalDate.now();
        currentGrouping = ReportsService.RevenueGrouping.DAILY;
        reportRangeLabel.setText(String.format("%s — %s",
                currentStart.format(displayDateFmt), currentEnd.format(displayDateFmt)));
        processSalesData(currentStart, currentEnd);
    }

    @FXML
    // Handles applyCustomDate logic
    private void applyCustomDate() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            return;
        }
        if (start.isAfter(end)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Período inválido");
            alert.setContentText("A data inicial deve ser anterior ou igual à data final.");
            alert.showAndWait();
            return;
        }

        periodGroup.selectToggle(null);
        currentStart = start;
        currentEnd = end;
        currentGrouping = ReportsService.RevenueGrouping.DAILY;
        reportRangeLabel.setText(String.format("%s — %s",
                start.format(displayDateFmt), end.format(displayDateFmt)));
        processSalesData(start, end);
    }

    // Handles selectPeriod logic
    private void selectPeriod(ToggleButton selected) {
        btnToday.getStyleClass().remove("period-active");
        btnWeek.getStyleClass().remove("period-active");
        btnMonth.getStyleClass().remove("period-active");
        selected.getStyleClass().add("period-active");
    }

    // Handles processSalesData logic
    private void processSalesData(LocalDate start, LocalDate end) {
        if (reportsService == null) {
            clearKPIs();
            return;
        }

        SalesReport report = reportsService.generateReport(start, end, currentGrouping);
        updateKPIUi(report);
        updateChartsUi(report);
        lastUpdatedLabel.setText("Atualizado: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    // Handles updateKPIUi logic
    private void updateKPIUi(SalesReport report) {
        kpiRevenue.setText(reportsService.formatCurrency(report.getTotalRevenue()));
        kpiTransactions.setText(String.valueOf(report.getTransactionCount()));

        double avgTicket = report.getTransactionCount() > 0
                ? report.getTotalRevenue() / report.getTransactionCount()
                : 0;
        kpiAvgTicket.setText("Ticket médio: " + reportsService.formatCurrency(avgTicket));
        kpiRevenueDelta.setText(report.getTransactionCount() > 0 ? "Período selecionado" : "Sem vendas no período");

        TopItem best = report.getBestSeller();
        if (best != null) {
            kpiBestItem.setText(best.name());
            kpiBestQty.setText(best.quantity() + " unidades");
        } else {
            kpiBestItem.setText("—");
            kpiBestQty.setText("0 unidades");
        }

        updateTop3(report.getTop3());
    }

    // Handles updateTop3 logic
    private void updateTop3(TopItem[] top3) {
        Label[] names = {top1Name, top2Name, top3Name};
        Label[] details = {top1Detail, top2Detail, top3Detail};

        for (int i = 0; i < 3; i++) {
            if (top3 != null && i < top3.length && top3[i] != null) {
                names[i].setText(top3[i].name());
                details[i].setText(String.format(Locale.forLanguageTag("pt-BR"),
                        "%d un  •  R$ %.2f", top3[i].quantity(), top3[i].revenue()));
            } else {
                names[i].setText("—");
                details[i].setText("0 un  •  R$ 0,00");
            }
        }
    }

    // Handles updateChartsUi logic
    private void updateChartsUi(SalesReport report) {
        revenueChart.getData().clear();
        revenueLineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> entry : report.getRevenueSeries().entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        revenueChart.getData().add(series);
        revenueLineChart.getData().add(copySeries(series));

        paymentPieChart.setData(FXCollections.observableArrayList());
        for (Map.Entry<String, Integer> entry : report.getPaymentBreakdown().entrySet()) {
            paymentPieChart.getData().add(new PieChart.Data(
                    translatePayment(entry.getKey()), entry.getValue()));
        }
    }

    // Handles clearKPIs logic
    private void clearKPIs() {
        kpiRevenue.setText("R$ 0,00");
        kpiTransactions.setText("0");
        kpiAvgTicket.setText("Ticket médio: R$ 0,00");
        kpiBestItem.setText("—");
        kpiBestQty.setText("0 unidades");
        clearTop3();
        revenueChart.getData().clear();
        revenueLineChart.getData().clear();
        paymentPieChart.setData(FXCollections.observableArrayList());
    }

    @FXML
    // Handles showBarChart logic
    private void showBarChart() {
        setChartType(false);
    }

    @FXML
    // Handles showLineChart logic
    private void showLineChart() {
        setChartType(true);
    }

    // Handles clearTop3 logic
    private void clearTop3() {
        updateTop3(new TopItem[0]);
    }

    // Handles translatePayment logic
    private String translatePayment(String method) {
        try {
            return switch (PaymentMethods.valueOf(method)) {
                case CASH -> "Dinheiro";
                case PIX -> "PIX";
                case CARD -> "Cartão";
            };
        } catch (IllegalArgumentException e) {
            return method;
        }
    }

    // Handles setChartType logic
    private void setChartType(boolean lineChartVisible) {
        revenueChart.setVisible(!lineChartVisible);
        revenueChart.setManaged(!lineChartVisible);
        revenueLineChart.setVisible(lineChartVisible);
        revenueLineChart.setManaged(lineChartVisible);
    }

    // Handles copySeries logic
    private XYChart.Series<String, Number> copySeries(XYChart.Series<String, Number> original) {
        XYChart.Series<String, Number> copy = new XYChart.Series<>();
        for (XYChart.Data<String, Number> data : original.getData()) {
            copy.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
        }
        return copy;
    }

    @FXML
    // Handles goToOrders logic
    private void goToOrders() {
        Main.changeScene("/fxml/order_entry.fxml");
    }

    @FXML
    // Handles goToInventory logic
    private void goToInventory() {
        Main.changeScene("/fxml/inventory.fxml");
    }

    @FXML
    // Handles goToReports logic
    private void goToReports() {
    }
}