package model;

// Each item of the order list
public class OrderItem{

    // Attributes
    private Product product;
    private int qtd;

    // Constructor

    public OrderItem(Product product, int qtd){
        this.product = product;
        this.qtd = qtd;
    }

    // Methods

    // Getters
    public int getQtd() {
        return qtd;
    }
    public Product getProduct() {
        return product;
    }
    public double getSubtotal(){ // OrderItem: [Product: Cappuccino, qtd: 3] -> cappuccino.getPrice * qtd
        return this.product.getPrice() * this.getQtd();
    }

    // Setters
    public void setProduct(Product product) {
        this.product = product;
    }
    public void setQtd(int qtd){
        if (qtd > 0){
            this.qtd = qtd;
        } else {
            System.out.println("FAIL: Quantity must be greater than 0.");
        }
    }
}