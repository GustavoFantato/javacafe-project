package model;

/**
 * Encapsulates a Product and its requested quantity within an Order.
 */
// Main class for orderitem related behavior
public class OrderItem {

    private Product product;
    private int qtd;

    // Handles OrderItem logic
    public OrderItem(Product product, int qtd){
        this.product = product;
        this.qtd = qtd;
    }

    // Handles getQtd logic
    public int getQtd() {
        return qtd;
    }

    // Handles getProduct logic
    public Product getProduct() {
        return product;
    }

    /**
     * @return The subtotal cost of this item (unit price * quantity).
     */
    // Handles getSubtotal logic
    public double getSubtotal(){
        return this.product.getPrice() * this.getQtd();
    }

    // Handles setProduct logic
    public void setProduct(Product product) {
        this.product = product;
    }

    // Handles setQtd logic
    public void setQtd(int qtd){
        if (qtd > 0){
            this.qtd = qtd;
        } else {
            System.out.println("[OrderItem] FAIL: Quantity must be greater than 0.");
        }
    }
}