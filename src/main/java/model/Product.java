package model;

public class Product{
    // Attributes
    private String name;
    private double price;
    private int stockQtd;
    private String imagePath;
    private String size;


    // Constructor
    public Product(String name, double price, int stockQtd, String imagePath, String size){
        this.name = name;
        this.price = price;
        this.stockQtd = stockQtd;
        this.imagePath = imagePath;
        this.size = size;
    }


    // Methods

    // Stock management
    public void decreaseStock(int qtd){
        this.stockQtd -= qtd;
    }
    public void increaseStock(int qtd){
        this.stockQtd += qtd;
    }


    // Getters
    public int getStockQtd() {
        return stockQtd;
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
    public String getSize() {
        return size;
    }

    // Setters
    public void setStockQtd(int stockQtd) {
        this.stockQtd = stockQtd;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public void setSize(String size) {
        this.size = size;
    }
}