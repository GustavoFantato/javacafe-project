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

/**
 * Entry point for the Java Cafe POS application.
 * Bootstraps the JavaFX environment and initializes core services.
 */

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
            // Ensures the data directory exists before writing any files
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            // Sets default view
            // When the project is started, it opens the main menu (order menu)
            changeScene("/fxml/order_entry.fxml");

            primaryStage.setTitle("Java Café POS"); // Application's title
            URL iconUrl = Main.class.getResource("/images/app-icon.png"); // Application's icon
            if (iconUrl != null) { //
                primaryStage.getIcons().setAll(new javafx.scene.image.Image(iconUrl.toExternalForm()));
            }
            primaryStage.setResizable(true); // It allows the user to resize the window
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(650);
            primaryStage.show(); // shows the application's window

        } catch (Exception e) { // exception handling
            System.err.println("[CRITICAL] Failed to load JavaFX GUI.");
            e.printStackTrace();
        }
    }

    /**
     * Swaps the current JavaFX scene and injects dependencies dynamically 
     * based on the loaded controller.
     * * @param fxmlPath The path to the FXML file to be loaded.
     */
    public static void changeScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath)); // gets the path
            BorderPane root = loader.load(); // reads the fxml and transforms in the objects

            // Dependency Injection mapping
            Object controller = loader.getController(); // instances the controller and vinculates it to the correct controller
            if (controller instanceof OrderEntryController) {
                ((OrderEntryController) controller).setServices(inventoryService, checkoutService);
            } else if (controller instanceof InventoryController) {
                ((InventoryController) controller).setService(inventoryService);
            } else if (controller instanceof ReportsController) {
                ((ReportsController) controller).setReportsService(reportsService);
            }

            // Setup scene only on the first load to prevent flickering
            // It reveals the scene only when it is full loaded on the background
            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
                scene.getStylesheets().add(Main.class.getResource("/css/style.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                primaryStage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Critical error navigating to screen: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Enforce US locale to avoid CSV parsing issues with decimal separators (',' vs '.')
        Locale.setDefault(Locale.US);

        System.out.println("=== INITIALIZING JAVA CAFE INFRASTRUCTURE ==="); // console logs

        // Objects instances
        inventoryService = new InventoryService(STORAGE_PATH);
        checkoutService = new CheckoutService(inventoryService, SALES_PATH);
        reportsService = new ReportsService(SALES_PATH);

        System.out.println("\n=== OPENING JAVAFX GUI ==="); // console logs
        launch(args); // when launch is called, the main thread pauses, and the JavaFX starts (and make everything it got to do)
    }
}