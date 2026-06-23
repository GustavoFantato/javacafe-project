package controller;

import exception.InvalidPaymentException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Order;
import model.OrderItem;
import model.Product;
import model.enums.Category;
import model.enums.Size;
import service.CheckoutService;
import service.InventoryService;
import service.ReceiptService;
import service.enums.FilterOperator;
import service.enums.PaymentMethods;
import util.ProductImageResolver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderEntryController {

    // All the FXML objects that were used
    @FXML private ToggleButton catAll;
    @FXML private ToggleButton catDrinks;
    @FXML private ToggleButton catFood;
    @FXML private ToggleButton catDessert;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterSizeCombo;
    @FXML private ComboBox<String> filterPriceOperatorCombo;
    @FXML private TextField filterPriceField;
    @FXML private Pagination menuPagination;
    @FXML private Label orderIdLabel;
    @FXML private TextField customerNameField;
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> colItemName;
    @FXML private TableColumn<OrderItem, Integer> colItemQty;
    @FXML private TableColumn<OrderItem, Double> colItemPrice;
    @FXML private TableColumn<OrderItem, Double> colItemTotal;
    @FXML private TableColumn<OrderItem, Void> colRemove;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label grandTotalLabel;
    @FXML private ComboBox<String> paymentCombo;
    @FXML private HBox cashRow;
    @FXML private TextField cashReceivedField;
    @FXML private Label changeLabel;
    @FXML private Label statusLabel;
    @FXML private Label clockLabel;
    @FXML private Label stockAlertLabel;

    // Services objects
    private InventoryService inventoryService;
    private CheckoutService checkoutService;
    private final ReceiptService receiptService = new ReceiptService();

    private Order currentOrder;
    private ObservableList<OrderItem> orderItemsObservableList;
    private Category currentCategoryFilter;
    private List<Product> filteredProducts = new ArrayList<>();

    private final ToggleGroup categoryToggleGroup = new ToggleGroup(); // It doesn't allow that more than one button get simultaneously pressed
    private static final int ITEMS_PER_PAGE = 6;
    private static final double MENU_GRID_WRAP_LENGTH = 720;
    private static final double MENU_CARD_WIDTH = 228;
    private static final double MENU_IMAGE_WIDTH = 204;
    private static final double MENU_IMAGE_HEIGHT = 114;

    @FXML
    public void initialize() {
        currentOrder = new Order(); // instances the order object
        orderItemsObservableList = FXCollections.observableArrayList(); // instances the order list object
        orderTable.setItems(orderItemsObservableList); // sets the orderTable with the observable (filtered/non-filtered) list

        // Order Table's columns
        colItemName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getProduct().getName()));
        colItemQty.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQtd()));
        colItemPrice.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getProduct().getPrice()));
        colItemTotal.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getSubtotal()));

        setupRemoveColumn(); // The column with the "X" button, to remove a specific orderItem

        // Category buttons, that isn't allowed to get clicked simultaneously
        catAll.setToggleGroup(categoryToggleGroup);
        catDrinks.setToggleGroup(categoryToggleGroup);
        catFood.setToggleGroup(categoryToggleGroup);
        catDessert.setToggleGroup(categoryToggleGroup);
        currentCategoryFilter = null;

        // When the cash payment method gets selected, this is set true
        cashRow.setVisible(false);
        cashRow.setManaged(false);

        // Payment box selection
        paymentCombo.getSelectionModel().selectedItemProperty().addListener(this::onPaymentChanged); // listener (when changed)
        // When the user inputs the cash value, it calculates and updates the change value on the screen
        cashReceivedField.textProperty().addListener((obs, oldVal, newVal) -> updateChange());

        filterSizeCombo.getSelectionModel().selectFirst(); // P, M, G filter
        filterPriceOperatorCombo.getSelectionModel().selectFirst(); // Price filter

        // Clock updated (screen's bottom)
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(30), e -> updateClock()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
        updateClock();

        refreshOrderInterface(); // It ensures that the screen is always showing the most updated version
    }

    // Services setters
    public void setServices(InventoryService inventoryService, CheckoutService checkoutService) {
        this.inventoryService = inventoryService;
        this.checkoutService = checkoutService;
        applyFilters();
        updateStockAlert();
    }

    @FXML
    // When the event happens, it applies the filter according to the category selected by the user
    private void filterCategory(ActionEvent event) {
        ToggleButton source = (ToggleButton) event.getSource();
        String userData = (String) source.getUserData();
        currentCategoryFilter = switch (userData) {
            case "DRINKS" -> Category.BEVERAGE;
            case "FOOD" -> Category.FOOD;
            case "DESSERTS" -> Category.DESSERTS;
            default -> null;
        };
        applyFilters(); // applies the filter
    }

    @FXML
    // search bar filter applier
    private void onSearch() {
        applyFilters();
    }

    //
    private void applyFilters() {
        if (inventoryService == null) {
            return;
        }

        // Products list instance
        List<Product> products = new ArrayList<>(inventoryService.getStorageList());

        if (currentCategoryFilter != null) { // if a specific category is selected, it may show only these category's products
            products = inventoryService.findProductByCategoryFilter(products, currentCategoryFilter); // gets the list filtered
        }

        String search = searchField.getText(); // search bar filter applier
        if (search != null && !search.isBlank()) { // if the search bar is not empty, it may search the specific product
            products = inventoryService.findProductByNameFilter(products, search);
        }

        String sizeSelection = filterSizeCombo.getSelectionModel().getSelectedItem(); // P, M, G filter applier
        if (sizeSelection != null && !sizeSelection.equals("Todos")) {
            products = inventoryService.findProductBySizeFilter(products, Size.valueOf(sizeSelection));
        }

        // Price filter applier
        String priceOp = filterPriceOperatorCombo.getSelectionModel().getSelectedItem();
        String priceText = filterPriceField.getText();
        if (priceOp != null && !priceOp.equals("Qualquer preço") && priceText != null && !priceText.isBlank()) {
            try {
                double price = Double.parseDouble(priceText.replace(",", ".").replace("R$", "").trim()); // price formatter
                FilterOperator operator = switch (priceOp) {
                    case "Até (≤)" -> FilterOperator.LESS_OR_EQUAL_THAN;
                    case "A partir de (≥)" -> FilterOperator.GREATER_OR_EQUAL_THAN;
                    case "Igual (=)" -> FilterOperator.EQUAL;
                    default -> FilterOperator.EQUAL;
                };
                products = inventoryService.findProductByPriceFilter(products, price, operator);
            } catch (NumberFormatException ignored) {
                // keeps the current list, if invalid price
            }
        }

        renderMenuCards(products); // render the filtered items list
    }

    // renders the item cards, according to the product's list received
    private void renderMenuCards(List<Product> products) {
        filteredProducts = new ArrayList<>(products);
        int pageCount = Math.max(1, (int) Math.ceil(filteredProducts.size() / (double) ITEMS_PER_PAGE));
        menuPagination.setPageCount(pageCount);
        menuPagination.setCurrentPageIndex(0);
        menuPagination.setPageFactory(this::createMenuPage);
    }

    private void addProductToOrder(Product product) {
        if (product.getStockQtd() <= 0) {
            showError("Estoque esgotado", "O produto \"" + product.getName() + "\" está sem estoque.");
            return;
        } // If no stock, it may show to the user

        // It verifies how many products are already in the cart, and sums
        int alreadyInCart = orderItemsObservableList.stream()
                .filter(i -> i.getProduct().getID() == product.getID())
                .mapToInt(OrderItem::getQtd)
                .sum();

        if (alreadyInCart + 1 > product.getStockQtd()) { // If there is no stock, it doesn't allow adding to the cart
            showError("Estoque insuficiente",
                    "Disponível: " + product.getStockQtd() + " un. Já no carrinho: " + alreadyInCart + ".");
            return;
        }

        currentOrder.addItem(new OrderItem(product, 1));
        refreshOrderInterface();
        statusLabel.setText("Adicionado: " + product.getName());
    }

    @FXML
    private void newOrder() { // new order setup
        currentOrder = new Order();
        customerNameField.clear();
        paymentCombo.getSelectionModel().clearSelection();
        cashReceivedField.clear();
        cashRow.setVisible(false);
        cashRow.setManaged(false);
        refreshOrderInterface();
        statusLabel.setText("Novo pedido iniciado.");
    }

    @FXML
    private void clearOrder() { // clear current order's items
        currentOrder.getItems().clear();
        refreshOrderInterface(); // refresh the clean list
        statusLabel.setText("Pedido limpo.");
    }

    @FXML
    private void finalizeOrder() { // order finalization
        if (currentOrder.getItems().isEmpty()) { // if empty cart, it doesn't allow to finalize
            showError("Carrinho vazio", "Adicione itens antes de finalizar a venda.");
            return;
        }

        String paymentLabel = paymentCombo.getSelectionModel().getSelectedItem();
        if (paymentLabel == null || paymentLabel.isBlank()) { // it may have a payment method to finalize
            showError("Pagamento", "Selecione uma forma de pagamento.");
            return;
        }

        PaymentMethods method = mapPaymentMethod(paymentLabel);
        double cashReceived = 0;

        if (method == PaymentMethods.CASH) { // if cash selected
            try {
                cashReceived = parseMoney(cashReceivedField.getText()); // formats the value
            } catch (NumberFormatException e) {
                showError("Valor inválido", "Informe um valor recebido válido."); // invalid value
                return;
            }
        }

        currentOrder.setCustomerName(customerNameField.getText()); // if the user opts to insert the name, it updates the name's field
        try {
            checkoutService.processPayment(method, currentOrder, cashReceived); // process the payment
            checkoutService.finishSale(currentOrder, method, cashReceived); // finish the sale

            double change = method == PaymentMethods.CASH
                    ? checkoutService.calculateChange(currentOrder.getTotalCost(), cashReceived)
                    : 0; // if the method selected is cash, it may calculate the change

            String receipt = receiptService.buildReceipt( // creates the receipt file
                    currentOrder, customerNameField.getText(), method, cashReceived, change);

            receiptService.offerReceiptActions(receipt,
                    (javafx.stage.Stage) orderTable.getScene().getWindow());

            statusLabel.setText("Venda finalizada com sucesso!");
            inventoryService.loadInventory();
            applyFilters();
            updateStockAlert();
            newOrder();

        } catch (InvalidPaymentException e) {
            showError("Pagamento recusado", e.getMessage());
        } catch (Exception e) {
            showError("Erro na venda", "Não foi possível finalizar a venda. Tente novamente.");
        }
    }

    // refresh the order interface
    private void refreshOrderInterface() {
        orderItemsObservableList.setAll(currentOrder.getItems());
        orderTable.refresh();
        orderIdLabel.setText(String.format(Locale.US, "#%03d", currentOrder.getOrderID()));

        subtotalLabel.setText(formatMoney(currentOrder.getListCost()));
        taxLabel.setText(formatMoney(currentOrder.getTaxCost()));
        grandTotalLabel.setText(formatMoney(currentOrder.getTotalCost()));
        updateChange();
    }

    // Listener when payment method gets changed
    private void onPaymentChanged(ObservableValue<? extends String> obs, String oldVal, String newVal) {
        handlePaymentSelection(newVal);
    }

    // Listener when payment method gets selected
    // If cash selected, the labels that weren't visible, become visible
    private void handlePaymentSelection(String selection) {
        boolean isCash = "Dinheiro".equals(selection);
        cashRow.setVisible(isCash);
        cashRow.setManaged(isCash);
        updateChange();
    }

    // updates the change value
    private void updateChange() {
        if (!"Dinheiro".equals(paymentCombo.getSelectionModel().getSelectedItem())) {
            changeLabel.setText(formatMoney(0));
            return;
        }

        try {
            double received = parseMoney(cashReceivedField.getText());
            double change = received - currentOrder.getTotalCost();
            changeLabel.setText(formatMoney(Math.max(0, change)));
        } catch (NumberFormatException e) {
            changeLabel.setText(formatMoney(0));
        }
    }

    // updates the stock alert, if necessary
    private void updateStockAlert() {
        if (inventoryService == null) {
            return;
        }

        List<Product> lowStock = inventoryService.getLowStockProducts();
        if (lowStock.isEmpty()) {
            stockAlertLabel.setText("");
        } else if (lowStock.size() == 1) {
            Product p = lowStock.get(0);
            stockAlertLabel.setText(String.format("Estoque baixo: %s (%d un.)", p.getName(), p.getStockQtd()));
        } else {
            stockAlertLabel.setText(String.format("%d produtos com estoque baixo", lowStock.size()));
        }
    }

    // setup the column with the "X"
    private void setupRemoveColumn() {
        colRemove.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("X");

            {
                removeBtn.getStyleClass().add("btn-ghost-small");
                removeBtn.setOnAction(e -> {
                    OrderItem item = getTableView().getItems().get(getIndex());
                    currentOrder.removeItem(item);
                    refreshOrderInterface();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
    }

    // Updated the clock
    private void updateClock() {
        clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    // creates the menu page
    private Node createMenuPage(Integer pageIndex) {
        FlowPane pageGrid = new FlowPane(12, 12);
        pageGrid.setPadding(new Insets(4));
        pageGrid.setPrefWrapLength(MENU_GRID_WRAP_LENGTH);

        int start = pageIndex * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, filteredProducts.size());

        if (filteredProducts.isEmpty()) {
            StackPane emptyPane = new StackPane(new Label("Nenhum produto encontrado."));
            emptyPane.getStyleClass().add("menu-empty-state");
            return emptyPane;
        }

        for (int i = start; i < end; i++) {
            pageGrid.getChildren().add(buildMenuCard(filteredProducts.get(i)));
        }

        return pageGrid;
    }

    // builds the product's menu card
    private VBox buildMenuCard(Product product) {
        VBox card = new VBox(8);
        card.getStyleClass().add("menu-card");
        card.setPadding(new Insets(12));
        card.setPrefWidth(MENU_CARD_WIDTH);

        ImageView imageView = new ImageView(ProductImageResolver.load(product.getImagePath(),
                MENU_IMAGE_WIDTH, MENU_IMAGE_HEIGHT));
        imageView.setFitWidth(MENU_IMAGE_WIDTH);
        imageView.setFitHeight(MENU_IMAGE_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.getStyleClass().add("menu-item-image");

        HBox imageRow = new HBox(imageView);
        imageRow.setAlignment(Pos.CENTER);
        imageRow.setMaxWidth(Double.MAX_VALUE);
        imageRow.getStyleClass().add("menu-item-image-wrap");

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("menu-item-name");
        nameLabel.setWrapText(true);

        Label descriptionLabel = new Label(product.getDescription());
        descriptionLabel.getStyleClass().add("menu-item-description");
        descriptionLabel.setWrapText(true);

        Label priceLabel = new Label(String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", product.getPrice()));
        priceLabel.getStyleClass().add("menu-item-price");

        Label stockLabel = new Label("Estoque: " + product.getStockQtd());
        stockLabel.getStyleClass().add("menu-item-stock");
        if (product.isLowStock()) {
            stockLabel.getStyleClass().add("menu-item-stock-low");
        }

        Button addBtn = new Button("+ Adicionar");
        addBtn.getStyleClass().add("btn-primary-small");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setDisable(product.getStockQtd() <= 0);
        addBtn.setOnAction(e -> addProductToOrder(product));

        card.getChildren().addAll(imageRow, nameLabel, descriptionLabel, priceLabel, stockLabel, addBtn);
        return card;
    }

    // maps the payment method
    private PaymentMethods mapPaymentMethod(String label) {
        if ("Dinheiro".equals(label)) {
            return PaymentMethods.CASH;
        }
        if ("PIX".equals(label)) {
            return PaymentMethods.PIX;
        }
        return PaymentMethods.CARD;
    }

    // parses the money
    private double parseMoney(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return Double.parseDouble(text.replace(",", ".").replace("R$", "").trim());
    }

    // formats the money
    private String formatMoney(double value) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", value);
    }


    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (orderTable.getScene() != null) {
            alert.initOwner(orderTable.getScene().getWindow());
        }
        alert.showAndWait();
        statusLabel.setText(message);
    }

    // scene changes
    @FXML
    private void goToOrders() {
    }

    @FXML
    private void goToInventory() {
        Main.changeScene("/fxml/inventory.fxml");
    }

    @FXML
    private void goToReports() {
        Main.changeScene("/fxml/reports.fxml");
    }
}
