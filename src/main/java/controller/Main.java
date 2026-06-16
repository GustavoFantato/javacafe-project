package controller;

import exception.InvalidPaymentException;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import model.Order;
import model.OrderItem;
import model.Product;
import service.CheckoutService;
import service.InventoryService;
import service.enums.PaymentMethods;

import java.util.List;

import static model.enums.Size.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Java Café POS - Pronto para começar!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("Java Café");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.out.println("=== INICIANDO TESTE DO CHECKOUT SERVICE ===");

        // 1. Criamos um InventoryService de mentira (Mock) apenas para o teste passar
        // Substitua pelo seu construtor real se ele pedir parâmetros!
        InventoryService mockInventory = new InventoryService("./data/storage.csv") {
            @Override
            public void decreaseProductStock(int id, int qtd) {
                System.out.println("[Mock Stock] Baixando " + qtd + " unidades do produto ID: " + id);
            }
        };

        // 2. Instanciamos o seu CheckoutService apontando para o arquivo de teste
        String caminhoCsv = "./data/sales.csv";
        CheckoutService checkoutService = new CheckoutService(mockInventory, caminhoCsv);

        // 3. Criamos alguns produtos fictícios para o teste
        Product cafeP = new Product(1, "Cappuccino - M", 3.50, 20,"cappuccino.png", M, "Café P");
        Product cafeM = new Product(1, "Cappuccino - P", 1.50, 20,"cappuccino.png", P, "Café G");

        // 4. Montamos o pedido (Order) usando a sua estrutura
        Order primeiroPedido = new Order();
        primeiroPedido.addItem(new OrderItem(cafeP, 2)); // 2 Cappuccinos P = R$ 10.00
        primeiroPedido.addItem(new OrderItem(cafeM, 1)); // 1 Cappuccino M = R$ 7.00
        // Total do pedido: R$ 17.00

        System.out.println("\n[Teste] Criado: " + primeiroPedido.toString());

        // 5. Simulamos o fluxo de pagamento em DINHEIRO (CASH) recebendo R$ 20.00
        try {
            System.out.println("\n[Teste] Processando pagamento...");
            boolean aprovado = checkoutService.processPayment(PaymentMethods.CASH, primeiroPedido, 20.00);

            if (aprovado) {
                System.out.println("[Teste] Sucesso! Finalizando a venda e gerando logs...");
                checkoutService.finishSale(primeiroPedido, PaymentMethods.CASH, 20.00);
            }

        } catch (InvalidPaymentException e) {
            System.err.println("[Teste] Erro esperado de pagamento: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[Teste] Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== FIM DO TESTE - ABRINDO INTERFACE JAVAFX ===");

        // Inicializa o ciclo de vida do JavaFX
        launch(args);
    }
}