package model;

import exception.OutOfStockException;
import model.enums.Category;
import model.enums.Size;

/**
 * Represents a sellable item inside the catalog and inventory system.
 */
// Main class for product related behavior
public class Product {
    public static final int DEFAULT_LOW_STOCK = 5;

    private String name;
    private Size size;
    private Category category;
    private double price;
    private int stockQtd;
    private int lowStockThreshold;
    private String imagePath;
    private String description;
    private static int idCounter = 0;
    private final int ID;

    public Product(int id, String name, double price, int stockQtd, String imagePath,
                   Size size, Category category, String description) {
        this(id, name, price, stockQtd, imagePath, size, category, description, DEFAULT_LOW_STOCK);
    }

    public Product(int id, String name, double price, int stockQtd, String imagePath,
                   Size size, Category category, String description, int lowStockThreshold) {
        this.name = name;
        this.price = price;
        this.stockQtd = stockQtd;
        this.imagePath = imagePath;
        this.size = size;
        this.category = category;
        this.description = description;
        this.lowStockThreshold = lowStockThreshold;
        this.ID = id;
        idCounter = Math.max(idCounter, id);
    }

    /**
     * Deducts units from the product's available stock.
     * * @param qtd Amount to be deducted.
     * @throws OutOfStockException If the required quantity exceeds current stock.
     */
    public void decreaseStock(int qtd) throws OutOfStockException {
        if (qtd > this.stockQtd) {
            throw new OutOfStockException("Estoque insuficiente para " + name + ". Disponível: " + stockQtd);
        }
        setStockQtd(this.stockQtd - qtd);
    }

    /**
     * Adds units to the product's available stock.
     * * @param qtd Amount to be added. Must be greater than zero.
     */
    // Handles increaseStock logic
    public void increaseStock(int qtd) {
        if (qtd > 0) {
            this.stockQtd += qtd;
        } else {
            System.out.println("[Product] FAIL: You cannot add a value less than or equal to 0 to stock.");
        }
    }

    // Handles isLowStock logic
    public boolean isLowStock() {
        return stockQtd <= lowStockThreshold;
    }

    // Handles getStockQtd logic
    public int getStockQtd() {
        return stockQtd;
    }

    // Handles getLowStockThreshold logic
    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    // Handles getName logic
    public String getName() {
        return name;
    }

    // Handles getPrice logic
    public double getPrice() {
        return price;
    }

    // Handles getImagePath logic
    public String getImagePath() {
        return imagePath;
    }

    // Handles getSize logic
    public Size getSize() {
        return size;
    }

    // Handles getCategory logic
    public Category getCategory() {
        return category;
    }

    // Handles getDescription logic
    public String getDescription() {
        return description;
    }

    // Handles getID logic
    public int getID() {
        return ID;
    }

    // Handles setStockQtd logic
    public void setStockQtd(int stockQtd) {
        this.stockQtd = stockQtd;
    }

    // Handles setLowStockThreshold logic
    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    // Handles setPrice logic
    public void setPrice(double price) {
        this.price = price;
    }

    // Handles setName logic
    public void setName(String name) {
        this.name = name;
    }

    // Handles setImagePath logic
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Handles setSize logic
    public void setSize(Size size) {
        this.size = size;
    }

    // Handles setCategory logic
    public void setCategory(Category category) {
        this.category = category;
    }

    // Handles setDescription logic
    public void setDescription(String description) {
        this.description = description;
    }
}