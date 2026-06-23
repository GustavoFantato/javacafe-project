package controller;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Product;
import model.enums.Category;
import model.enums.Size;
import service.InventoryService;
import util.ProductImageResolver;
import util.ProductImageStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InventoryController {

    @FXML private TextField inventorySearchField;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private HBox alertBanner;
    @FXML private Label alertBannerText;
    @FXML private TableView<Product> inventoryTable;
    @FXML private TableColumn<Product, Integer> colProductId;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, String> colProductCategory;
    @FXML private TableColumn<Product, Double> colProductPrice;
    @FXML private TableColumn<Product, Integer> colProductStock;
    @FXML private TableColumn<Product, Integer> colProductThreshold;
    @FXML private TableColumn<Product, String> colProductStatus;
    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockCountLabel;
    @FXML private ToggleButton tabAddProduct;
    @FXML private ToggleButton tabEditProduct;
    @FXML private VBox addProductPane;
    @FXML private VBox editProductPane;
    @FXML private TextField newNameField;
    @FXML private ComboBox<String> newCategoryCombo;
    @FXML private TextField newPriceField;
    @FXML private TextField newStockField;
    @FXML private Spinner<Integer> newThresholdSpinner;
    @FXML private TextArea newDescField;
    @FXML private Label selectedProductLabel;
    @FXML private TextField editNameField;
    @FXML private TextField editPriceField;
    @FXML private TextField editImageField;
    @FXML private Spinner<Integer> editThresholdSpinner;
    @FXML private Spinner<Integer> restockSpinner;
    @FXML private Label currentStockLabel;
    @FXML private ImageView editImagePreview;
    @FXML private Label formStatusLabel;
    @FXML private Label statusLabel;
    @FXML private Label clockLabel;

    private InventoryService inventoryService;
    private ObservableList<Product> productObservableList;
    private Product selectedProduct;

    private final ToggleGroup formTabs = new ToggleGroup();

    @FXML
    public void initialize() {
        productObservableList = FXCollections.observableArrayList();
        inventoryTable.setItems(productObservableList);

        colProductId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getID()));
        colProductName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colProductCategory.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(translateCategoryToPt(data.getValue().getCategory())));
        colProductPrice.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        colProductStock.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getStockQtd()));
        colProductThreshold.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getLowStockThreshold()));
        colProductStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().isLowStock() ? "Baixo" : "OK"));

        tabAddProduct.setToggleGroup(formTabs);
        tabEditProduct.setToggleGroup(formTabs);

        SpinnerValueFactory<Integer> newThresholdFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, Product.DEFAULT_LOW_STOCK);
        newThresholdSpinner.setValueFactory(newThresholdFactory);

        SpinnerValueFactory<Integer> editThresholdFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, Product.DEFAULT_LOW_STOCK);
        editThresholdSpinner.setValueFactory(editThresholdFactory);

        SpinnerValueFactory<Integer> restockFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 10);
        restockSpinner.setValueFactory(restockFactory);

        categoryFilterCombo.getSelectionModel().selectFirst();

        ChangeListener<Product> selectionListener = (obs, oldVal, newVal) -> {
            selectedProduct = newVal;
            if (newVal != null) {
                loadProductIntoEditForm(newVal);
            }
        };
        inventoryTable.getSelectionModel().selectedItemProperty().addListener(selectionListener);

        editImageField.textProperty().addListener((obs, oldValue, newValue) -> refreshImagePreview(newValue));

        javafx.animation.Timeline clock = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(30), e -> updateClock()));
        clock.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        clock.play();
        updateClock();
    }

    public void setService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        refreshTableAndMetrics();
    }

    private void refreshTableAndMetrics() {
        if (inventoryService == null) {
            return;
        }

        applyFilters();
        inventoryTable.refresh();

        int total = productObservableList.size();
        int lowCount = 0;
        for (Product p : productObservableList) {
            if (p.isLowStock()) {
                lowCount++;
            }
        }

        totalProductsLabel.setText(String.format("Total: %d produtos", total));
        lowStockCountLabel.setText(String.format("Estoque baixo: %d", lowCount));

        if (lowCount > 0) {
            alertBannerText.setText(String.format("Atenção: %d item(ns) com estoque baixo!", lowCount));
            alertBanner.setVisible(true);
            alertBanner.setManaged(true);
        } else {
            alertBanner.setVisible(false);
            alertBanner.setManaged(false);
        }
    }

    @FXML
    private void onSearch() {
        refreshTableAndMetrics();
    }

    @FXML
    private void onCategoryFilter() {
        refreshTableAndMetrics();
    }

    private void applyFilters() {
        List<Product> products = new ArrayList<>(inventoryService.getStorageList());

        String search = inventorySearchField.getText();
        if (search != null && !search.isBlank()) {
            products = inventoryService.findProductByNameFilter(products, search);
        }

        String categoryLabel = categoryFilterCombo.getSelectionModel().getSelectedItem();
        if (categoryLabel != null && !categoryLabel.equals("Todos")) {
            products = inventoryService.findProductByCategoryFilter(products, mapPtToCategoryEnum(categoryLabel));
        }

        productObservableList.setAll(products);
    }

    @FXML
    private void switchToAdd() {
        addProductPane.setVisible(true);
        addProductPane.setManaged(true);
        editProductPane.setVisible(false);
        editProductPane.setManaged(false);
        tabAddProduct.getStyleClass().add("form-tab-active");
        tabEditProduct.getStyleClass().remove("form-tab-active");
    }

    @FXML
    private void switchToEdit() {
        editProductPane.setVisible(true);
        editProductPane.setManaged(true);
        addProductPane.setVisible(false);
        addProductPane.setManaged(false);
        tabEditProduct.getStyleClass().add("form-tab-active");
        tabAddProduct.getStyleClass().remove("form-tab-active");
    }

    @FXML
    private void addProduct() {
        String name = newNameField.getText();
        String categoryLabel = newCategoryCombo.getSelectionModel().getSelectedItem();
        String priceText = newPriceField.getText();
        String stockText = newStockField.getText();

        if (name == null || name.isBlank() || categoryLabel == null || priceText == null || priceText.isBlank()
                || stockText == null || stockText.isBlank()) {
            formStatusLabel.setText("Erro: Preencha todos os campos obrigatórios (*)");
            return;
        }

        try {
            double price = Double.parseDouble(priceText.replace(",", "."));
            int stock = Integer.parseInt(stockText.trim());
            int threshold = newThresholdSpinner.getValue();
            int newId = inventoryService.getNextProductId();

            String description = newDescField.getText() != null ? newDescField.getText().trim() : "";

            Product product = new Product(
                    newId, name.trim(), price, stock, ProductImageResolver.defaultImagePath(),
                    Size.M, mapPtToCategoryEnum(categoryLabel), description, threshold);

            inventoryService.addProductStorage(product);
            formStatusLabel.setText("Produto adicionado com sucesso!");
            clearAddForm();
            refreshTableAndMetrics();
        } catch (NumberFormatException e) {
            formStatusLabel.setText("Erro: Preço ou Estoque inválidos!");
        }
    }

    @FXML
    private void clearAddForm() {
        newNameField.clear();
        newCategoryCombo.getSelectionModel().clearSelection();
        newPriceField.clear();
        newStockField.clear();
        newThresholdSpinner.getValueFactory().setValue(Product.DEFAULT_LOW_STOCK);
        newDescField.clear();
    }

    private void loadProductIntoEditForm(Product product) {
        selectedProductLabel.setText(product.getName());
        editNameField.setText(product.getName());
        editPriceField.setText(String.format(Locale.US, "%.2f", product.getPrice()));
        editImageField.setText(product.getImagePath());
        editThresholdSpinner.getValueFactory().setValue(product.getLowStockThreshold());
        currentStockLabel.setText(String.valueOf(product.getStockQtd()));
        refreshImagePreview(product.getImagePath());
    }

    @FXML
    private void saveEdits() {
        if (selectedProduct == null) {
            formStatusLabel.setText("Selecione um produto na tabela.");
            return;
        }

        try {
            double price = Double.parseDouble(editPriceField.getText().replace(",", "."));
            int threshold = editThresholdSpinner.getValue();
            String imagePath = resolveEditedImagePath();

            Product updated = new Product(
                    selectedProduct.getID(),
                    editNameField.getText().trim(),
                    price,
                    selectedProduct.getStockQtd(),
                    imagePath,
                    selectedProduct.getSize(),
                    selectedProduct.getCategory(),
                    selectedProduct.getDescription(),
                    threshold);

            inventoryService.updateProductStorage(updated);
            editImageField.setText(imagePath);
            refreshImagePreview(imagePath);
            formStatusLabel.setText("Alterações salvas!");
            refreshTableAndMetrics();
        } catch (NumberFormatException e) {
            formStatusLabel.setText("Erro: Preço inválido!");
        } catch (IOException e) {
            formStatusLabel.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    private void restockProduct() {
        if (selectedProduct == null) {
            formStatusLabel.setText("Selecione um produto na tabela.");
            return;
        }

        int amount = restockSpinner.getValue();
        inventoryService.increaseProductStock(selectedProduct.getID(), amount);
        inventoryService.loadInventory();
        selectedProduct = inventoryService.findProductById(selectedProduct.getID());
        if (selectedProduct != null) {
            loadProductIntoEditForm(selectedProduct);
        }
        formStatusLabel.setText(String.format("+%d unidades adicionadas ao estoque.", amount));
        refreshTableAndMetrics();
    }

    @FXML
    private void removeProduct() {
        if (selectedProduct == null) {
            formStatusLabel.setText("Selecione um produto na tabela.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remover produto");
        confirm.setHeaderText("Remover \"" + selectedProduct.getName() + "\"?");
        confirm.setContentText("Esta ação não pode ser desfeita.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            inventoryService.removeProductStorage(selectedProduct.getID());
            selectedProduct = null;
            selectedProductLabel.setText("—");
            editNameField.clear();
            editPriceField.clear();
            currentStockLabel.setText("—");
            formStatusLabel.setText("Produto removido!");
            refreshTableAndMetrics();
        }
    }

    @FXML
    private void scrollToLowStock() {
        for (Product p : productObservableList) {
            if (p.isLowStock()) {
                inventoryTable.getSelectionModel().select(p);
                inventoryTable.scrollTo(p);
                switchToEdit();
                return;
            }
        }
    }

    @FXML
    private void exportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar backup do estoque");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo CSV", "*.csv"));
        chooser.setInitialFileName("backup_estoque_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv");

        Stage stage = (Stage) inventoryTable.getScene().getWindow();
        File targetFile = chooser.showSaveDialog(stage);
        if (targetFile == null) {
            formStatusLabel.setText("Exportacao cancelada.");
            return;
        }

        try {
            Files.copy(new File(inventoryService.getFilePath()).toPath(),
                    targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            formStatusLabel.setText("Backup salvo em: " + targetFile.getAbsolutePath());
        } catch (Exception e) {
            formStatusLabel.setText("Erro ao exportar CSV.");
        }
    }

    @FXML
    private void browseEditImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar imagem do produto");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

        Stage stage = (Stage) inventoryTable.getScene().getWindow();
        File selectedFile = chooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        editImageField.setText(selectedFile.toURI().toString());
        formStatusLabel.setText("Imagem selecionada para o produto.");
    }

    private Category mapPtToCategoryEnum(String label) {
        return switch (label) {
            case "Bebidas" -> Category.BEVERAGE;
            case "Comidas" -> Category.FOOD;
            case "Sobremesas" -> Category.DESSERTS;
            default -> Category.MISC;
        };
    }

    private String translateCategoryToPt(Category category) {
        return switch (category) {
            case BEVERAGE -> "Bebidas";
            case FOOD -> "Comidas";
            case DESSERTS -> "Sobremesas";
            default -> "Geral";
        };
    }

    private void updateClock() {
        clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private String resolveEditedImagePath() throws IOException {
        String imagePath = editImageField.getText();
        if (imagePath == null || imagePath.isBlank()) {
            return ProductImageResolver.defaultImagePath();
        }

        String normalizedPath = imagePath.trim();
        if (ProductImageStorage.isInternalPath(normalizedPath)) {
            return normalizedPath.startsWith("/") ? normalizedPath.substring(1) : normalizedPath;
        }

        return ProductImageStorage.importProductImage(
                ProductImageStorage.resolveSourcePath(normalizedPath),
                selectedProduct.getID(),
                editNameField.getText().trim());
    }

    private void refreshImagePreview(String imagePath) {
        editImagePreview.setImage(ProductImageResolver.load(imagePath, 220, 140));
    }

    @FXML
    private void goToOrders() {
        Main.changeScene("/fxml/order_entry.fxml");
    }

    @FXML
    private void goToInventory() {
        // já nesta tela
    }

    @FXML
    private void goToReports() {
        Main.changeScene("/fxml/reports.fxml");
    }
}
