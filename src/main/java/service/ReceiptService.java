package service;

import model.Order;
import model.OrderItem;
import service.enums.PaymentMethods;

import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class ReceiptService {

    private static final DateTimeFormatter RECEIPT_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public String buildReceipt(Order order, String customerName, PaymentMethods paymentMethod,
                               double cashReceived, double change) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           ☕ JAVA CAFÉ\n");
        sb.append("========================================\n");
        sb.append(String.format(Locale.US, "Pedido: #%03d%n", order.getOrderID()));
        sb.append("Data: ").append(LocalDateTime.now().format(RECEIPT_TIME)).append("\n");

        if (customerName != null && !customerName.isBlank()) {
            sb.append("Cliente: ").append(customerName.trim()).append("\n");
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("%-22s %4s %8s%n", "Item", "Qtd", "Total"));
        sb.append("----------------------------------------\n");

        for (OrderItem item : order.getItems()) {
            sb.append(String.format(Locale.US, "%-22s %4d %8.2f%n",
                    truncate(item.getProduct().getName(), 22),
                    item.getQtd(),
                    item.getSubtotal()));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format(Locale.US, "%28s %8.2f%n", "Subtotal:", order.getListCost()));
        sb.append(String.format(Locale.US, "%28s %8.2f%n", "Impostos (10%):", order.getTaxCost()));
        sb.append(String.format(Locale.US, "%28s %8.2f%n", "TOTAL:", order.getTotalCost()));
        sb.append("----------------------------------------\n");
        sb.append("Pagamento: ").append(translatePayment(paymentMethod)).append("\n");

        if (paymentMethod == PaymentMethods.CASH) {
            sb.append(String.format(Locale.US, "Valor recebido: R$ %.2f%n", cashReceived));
            sb.append(String.format(Locale.US, "Troco: R$ %.2f%n", change));
        }

        sb.append("========================================\n");
        sb.append("     Obrigado pela preferência!\n");
        sb.append("========================================\n");

        return sb.toString();
    }

    public void offerReceiptActions(String receiptContent, Stage owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Venda finalizada");
        alert.setHeaderText("Deseja salvar ou imprimir o cupom?");
        alert.setContentText("A venda foi registrada com sucesso.");
        alert.getButtonTypes().setAll(
                new ButtonType("Salvar"),
                new ButtonType("Imprimir"),
                new ButtonType("Fechar", ButtonType.CANCEL.getButtonData())
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String choice = result.get().getText();
        if ("Salvar".equals(choice)) {
            saveReceipt(receiptContent, owner);
        } else if ("Imprimir".equals(choice)) {
            printReceipt(receiptContent, owner);
        }
    }

    public void saveReceipt(String content, Stage owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar cupom");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo de texto", "*.txt"));
        chooser.setInitialFileName(String.format(Locale.US, "cupom_%d.txt",
                System.currentTimeMillis()));

        File file = chooser.showSaveDialog(owner);
        if (file == null) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            showInfo(owner, "Cupom salvo", "Arquivo salvo em:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showError(owner, "Erro ao salvar", "Não foi possível salvar o cupom.");
        }
    }

    public void printReceipt(String content, Stage owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null || !job.showPrintDialog(owner)) {
            showError(owner, "Impressão", "Nenhuma impressora disponível ou operação cancelada.");
            return;
        }

        javafx.scene.text.Text text = new javafx.scene.text.Text(content);
        text.setStyle("-fx-font-family: monospace; -fx-font-size: 10;");
        boolean success = job.printPage(text);
        if (success) {
            job.endJob();
            showInfo(owner, "Impressão", "Cupom enviado para a impressora.");
        } else {
            job.endJob();
            showError(owner, "Impressão", "Falha ao imprimir o cupom.");
        }
    }

    private String translatePayment(PaymentMethods method) {
        return switch (method) {
            case CASH -> "Dinheiro";
            case PIX -> "PIX";
            case CARD -> "Cartão";
        };
    }

    private String truncate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 1) + "…";
    }

    private void showInfo(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }

    private void showError(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
}
