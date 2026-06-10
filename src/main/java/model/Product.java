package model;

import exception.OutOfStockException;

public class Product{
    // Attributes
    private String name;
    private double price;
    private int stockQtd;
    private String imagePath;
    private Size size;


    // Constructor
    public Product(String name, double price, int stockQtd, String imagePath, Size size){
        this.name = name;
        this.price = price;
        this.stockQtd = stockQtd;
        this.imagePath = imagePath;
        this.size = size;
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

    // Setters
    private void setStockQtd(int stockQtd) {
        this.stockQtd = stockQtd;
    }
    private void setPrice(double price) {
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
}