package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
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

    private ReportsService reportsService;
    private LocalDate currentStart;
    private LocalDate currentEnd;

    private final ToggleGroup periodGroup = new ToggleGroup();
    private final DateTimeFormatter displayDateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        btnToday.setToggleGroup(periodGroup);
        btnWeek.setToggleGroup(periodGroup);
        btnMonth.setToggleGroup(periodGroup);
        periodGroup.selectToggle(btnToday);

        revenueChart.setLegendVisible(false);
        paymentPieChart.setLabelsVisible(true);
    }

    public void setReportsService(ReportsService reportsService) {
        this.reportsService = reportsService;
        loadToday();
    }

    public void setSalesFilePath(String salesFilePath) {
        this.reportsService = new ReportsService(salesFilePath);
        loadToday();
    }

    @FXML
    public void refreshReport() {
        if (currentStart != null && currentEnd != null) {
            processSalesData(currentStart, currentEnd);
        } else {
            loadToday();
        }
    }

    @FXML
    private void loadToday() {
        selectPeriod(btnToday);
        currentStart = LocalDate.now();
        currentEnd = LocalDate.now();
        reportRangeLabel.setText("Hoje — " + currentStart.format(displayDateFmt));
        processSalesData(currentStart, currentEnd);
    }

    @FXML
    private void loadWeek() {
        selectPeriod(btnWeek);
        currentStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        currentEnd = LocalDate.now();
        reportRangeLabel.setText(String.format("%s — %s",
                currentStart.format(displayDateFmt), currentEnd.format(displayDateFmt)));
        processSalesData(currentStart, currentEnd);
    }

    @FXML
    private void loadMonth() {
        selectPeriod(btnMonth);
        currentStart = LocalDate.now().withDayOfMonth(1);
        currentEnd = LocalDate.now();
        reportRangeLabel.setText(String.format("%s — %s",
                currentStart.format(displayDateFmt), currentEnd.format(displayDateFmt)));
        processSalesData(currentStart, currentEnd);
    }

    @FXML
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
        reportRangeLabel.setText(String.format("%s — %s",
                start.format(displayDateFmt), end.format(displayDateFmt)));
        processSalesData(start, end);
    }

    private void selectPeriod(ToggleButton selected) {
        btnToday.getStyleClass().remove("period-active");
        btnWeek.getStyleClass().remove("period-active");
        btnMonth.getStyleClass().remove("period-active");
        selected.getStyleClass().add("period-active");
    }

    private void processSalesData(LocalDate start, LocalDate end) {
        if (reportsService == null) {
            clearKPIs();
            return;
        }

        SalesReport report = reportsService.generateReport(start, end);
        updateKPIUi(report);
        updateChartsUi(report);
        lastUpdatedLabel.setText("Atualizado: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

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

    private void updateChartsUi(SalesReport report) {
        revenueChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Double> entry : report.getDailyRevenue().entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        revenueChart.getData().add(series);

        paymentPieChart.setData(FXCollections.observableArrayList());
        for (Map.Entry<String, Integer> entry : report.getPaymentBreakdown().entrySet()) {
            paymentPieChart.getData().add(new PieChart.Data(
                    translatePayment(entry.getKey()), entry.getValue()));
        }
    }

    private void clearKPIs() {
        kpiRevenue.setText("R$ 0,00");
        kpiTransactions.setText("0");
        kpiAvgTicket.setText("Ticket médio: R$ 0,00");
        kpiBestItem.setText("—");
        kpiBestQty.setText("0 unidades");
        clearTop3();
        revenueChart.getData().clear();
        paymentPieChart.setData(FXCollections.observableArrayList());
    }

    private void clearTop3() {
        updateTop3(new TopItem[0]);
    }

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

    @FXML
    private void goToOrders() {
        Main.changeScene("/fxml/order_entry.fxml");
    }

    @FXML
    private void goToInventory() {
        Main.changeScene("/fxml/inventory.fxml");
    }

    @FXML
    private void goToReports() {
        // já nesta tela
    }
}
