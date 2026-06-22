package model;

import exception.OutOfStockException;
import model.enums.Category;
import model.enums.Size;

public class Product {
    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

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
        this(id, name, price, stockQtd, imagePath, size, category, description, DEFAULT_LOW_STOCK_THRESHOLD);
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

    public void decreaseStock(int qtd) throws OutOfStockException {
        if (qtd > this.stockQtd) {
            throw new OutOfStockException("Estoque insuficiente para " + name + ". Disponível: " + stockQtd);
        }
        setStockQtd(this.stockQtd - qtd);
    }

    public void increaseStock(int qtd) {
        if (qtd > 0) {
            this.stockQtd += qtd;
        } else {
            System.out.println("FAIL: You cannot add a value less than or equal to 0 to stock.");
        }
    }

    public boolean isLowStock() {
        return stockQtd <= lowStockThreshold;
    }

    public int getStockQtd() {
        return stockQtd;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Size getSize() {
        return size;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getID() {
        return ID;
    }

    public void setStockQtd(int stockQtd) {
        this.stockQtd = stockQtd;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
