package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import service.CheckoutService;
import service.InventoryService;
import service.ReportsService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

public class Main extends Application {

    private static InventoryService inventoryService;
    private static CheckoutService checkoutService;
    private static ReportsService reportsService;
    private static Stage primaryStage;
    private static final String STORAGE_PATH = "./data/storage.csv";
    private static final String SALES_PATH = "./data/sales.csv";
    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 1000;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            // Garante a existência do diretório de dados
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            // Abre a tela inicial padrão (Pedidos)
            changeScene("/fxml/order_entry.fxml");

            primaryStage.setTitle("Java Café POS");
            URL iconUrl = Main.class.getResource("/images/app-icon.png");
            if (iconUrl != null) {
                primaryStage.getIcons().setAll(new javafx.scene.image.Image(iconUrl.toExternalForm()));
            }
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("CRITICAL: Falha ao carregar a interface gráfica do JavaFX.");
            e.printStackTrace();
        }
    }

    /**
     * Troca de cena injetando dinamicamente as dependências dos microsserviços
     */
    public static void changeScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            BorderPane root = loader.load();

            // Injeta as dependências necessárias no controller que acabou de ser inflado
            Object controller = loader.getController();
            if (controller instanceof OrderEntryController) {
                ((OrderEntryController) controller).setServices(inventoryService, checkoutService);
            } else if (controller instanceof InventoryController) {
                ((InventoryController) controller).setService(inventoryService);
            } else if (controller instanceof ReportsController) {
                ((ReportsController) controller).setReportsService(reportsService);
            }

            // Se for a primeira inicialização
            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
                // Vincula o arquivo de estilo higienizado de forma segura
                scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                // Reaproveita a Scene alterando apenas o nó raiz (evita flickering e perda de foco)
                primaryStage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            System.err.println("Erro crítico ao navegar para a tela: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Evita quebras de parseamento decimal no CSV (força ponto ao invés de vírgula)
        Locale.setDefault(Locale.US);

        System.out.println("=== INICIANDO INFRAESTRUTURA DO JAVA CAFE ===");

        // Inicialização dos serviços globais
        inventoryService = new InventoryService(STORAGE_PATH);
        checkoutService = new CheckoutService(inventoryService, SALES_PATH);
        reportsService = new ReportsService(SALES_PATH);

        System.out.println("\n=== ABRINDO INTERFACE GRAFICA JAVAFX ===");
        launch(args);
    }
}