package model;

import exception.OutOfStockException;
import model.enums.Size;

public class Product{
    // Attributes
    private String name;
    private Size size;
    private double price;
    private int stockQtd;
    private String imagePath;
    private String description;
    private static int idCounter = 0;
    private final int ID;

    // Constructor
    public Product(int ID, String name, double price, int stockQtd, String imagePath, Size size, String description){
        this.name = name;
        this.price = price;
        this.stockQtd = stockQtd;
        this.imagePath = imagePath;
        this.size = size;
        this.description = description;
        this.ID = ++idCounter;
    }


    // Methods

    // Stock management
    public void decreaseStock(int qtd) throws OutOfStockException{

        if(qtd > this.stockQtd){
            throw new OutOfStockException("ERROR: No enough stock!"); // The code stops here if the exception occurs
        }

        setStockQtd((this.stockQtd - qtd));
    }

    public void increaseStock(int qtd){
        if(qtd > 0){
            this.stockQtd += qtd;
        } else {
            System.out.println("FAIL: You cannot add a value less than or equal to 0 to stock.");
        }
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
    public Size getSize() {
        return size;
    }
    public String getDescription() {
        return description;
    }
    public int getID() {
        return ID;
    }

    // Setters
    public void setStockQtd(int stockQtd) {
        this.stockQtd = stockQtd;
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
    public void setDescription(String description) {
        this.description = description;
    }
}