package model;

/**
 * Encapsulates a Product and its requested quantity within an Order.
 */
public class OrderItem {

    private Product product;
    private int qtd;

    public OrderItem(Product product, int qtd){
        this.product = product;
        this.qtd = qtd;
    }

    public int getQtd() {
        return qtd;
    }

    public Product getProduct() {
        return product;
    }

    /**
     * @return The subtotal cost of this item (unit price * quantity).
     */
    public double getSubtotal(){ 
        return this.product.getPrice() * this.getQtd();
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQtd(int qtd){
        if (qtd > 0){
            this.qtd = qtd;
        } else {
            System.out.println("[OrderItem] FAIL: Quantity must be greater than 0.");
        }
    }
}