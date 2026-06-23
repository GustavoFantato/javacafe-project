package service;

import model.enums.Size;
import model.enums.Category;
import model.Product;
import service.enums.FilterOperator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the product catalog, stock levels, and handles inventory CSV persistence.
 */
public class InventoryService {

    private static final String CSV_HEADER =
            "id,name,size,category,price,stock,lowStockThreshold,imagePath,description";

    private List<Product> storageList;
    private final String filePath;

    public InventoryService(String filePath) {
        this.filePath = filePath;
        this.storageList = new ArrayList<>();
        loadInventory();
    }

    /**
     * Loads the entire inventory from the storage CSV into memory.
     */
    public void loadInventory() {
        this.storageList.clear();

        File file = new File(this.filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(this.filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                Product product = parseLine(line);
                if (product != null) {
                    this.storageList.add(product);
                }
            }

        } catch (Exception e) {
            System.err.println("[Inventory] An error occurred while reading storage file: " + e.getMessage());
        }
    }

    /**
     * Overwrites the CSV file with the current state of the inventory list in memory.
     */
    public void saveInventory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath))) {
            writer.write(CSV_HEADER);
            writer.newLine();

            for (Product p : storageList) {
                String line = String.format(java.util.Locale.US, "%d,%s,%s,%s,%.2f,%d,%d,%s,\"%s\"",
                        p.getID(),
                        p.getName(),
                        p.getSize().name(),
                        p.getCategory().name(),
                        p.getPrice(),
                        p.getStockQtd(),
                        p.getLowStockThreshold(),
                        p.getImagePath(),
                        p.getDescription()
                );

                writer.write(line);
                writer.newLine();
            }
            System.out.printf("[Inventory] Storage saved successfully [%s]!\n", filePath);

        } catch (Exception e) {
            System.err.println("[Inventory] An error occurred while writing storage file: " + e.getMessage());
        }
    }

    public int getNextProductId() {
        int maxId = 0;
        for (Product p : storageList) {
            maxId = Math.max(maxId, p.getID());
        }
        return maxId + 1;
    }

    public List<Product> getLowStockProducts() {
        List<Product> lowStock = new ArrayList<>();
        for (Product p : storageList) {
            if (p.isLowStock()) {
                lowStock.add(p);
            }
        }
        return lowStock;
    }

    public void decreaseProductStock(int id, int qtd) throws exception.OutOfStockException {
        Product p = findProductById(id);

        if (p != null) {
            p.decreaseStock(qtd);
            saveInventory();
        } else {
            throw new exception.OutOfStockException("Produto com ID " + id + " não encontrado.");
        }
    }

    public void increaseProductStock(int id, int qtd) {
        Product p = findProductById(id);

        if (p != null) {
            p.increaseStock(qtd);
            saveInventory();
        } else {
            System.err.println("[Inventory] FAIL: Product with ID " + id + " not found!");
        }
    }

    public List<Product> findProductByCategoryFilter(List<Product> baseList, Category category) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : baseList) {
            if (p.getCategory() == category) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public List<Product> findProductByNameFilter(List<Product> baseList, String name) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : baseList) {
            if (p.getName().toLowerCase().contains(name.toLowerCase().trim())) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public List<Product> findProductBySizeFilter(List<Product> baseList, Size size) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : baseList) {
            if (p.getSize() == size) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    public List<Product> findProductByPriceFilter(List<Product> baseList, double price, FilterOperator operator) {
        List<Product> filtered = new ArrayList<>();

        for (Product p : baseList) {
            switch (operator) {
                case LESS_OR_EQUAL_THAN:
                    if (p.getPrice() <= price) {
                        filtered.add(p);
                    }
                    break;
                case GREATER_OR_EQUAL_THAN:
                    if (p.getPrice() >= price) {
                        filtered.add(p);
                    }
                    break;
                case EQUAL:
                    if (p.getPrice() == price) {
                        filtered.add(p);
                    }
                    break;
            }
        }

        return filtered;
    }

    public void addProductStorage(Product newProd) {
        if (isWellFormatedProduct(newProd)) {
            storageList.add(newProd);
            saveInventory();
            System.out.println("[Inventory] LOG: Product added successfully!");
        } else {
            System.out.println("[Inventory] FAIL: Bad formatted product!");
        }
    }

    public void removeProductStorage(int id) {
        Product p = findProductById(id);

        if (p != null) {
            storageList.remove(p);
            saveInventory();
            System.out.println("[Inventory] LOG: Product removed successfully!");
        } else {
            System.err.println("[Inventory] FAIL: Product not found!");
        }
    }

    public void updateProductStorage(Product updatedProd) {
        if (!isWellFormatedProduct(updatedProd)) {
            System.err.println("[Inventory] FAIL: Cannot update. Product contains formatting errors!");
            return;
        }

        Product oldProd = findProductById(updatedProd.getID());

        if (oldProd == null) {
            System.err.println("[Inventory] FAIL: Cannot update. Product ID " + updatedProd.getID() + " not found.");
            return;
        }

        oldProd.setName(updatedProd.getName());
        oldProd.setSize(updatedProd.getSize());
        oldProd.setCategory(updatedProd.getCategory());
        oldProd.setPrice(updatedProd.getPrice());
        oldProd.setStockQtd(updatedProd.getStockQtd());
        oldProd.setLowStockThreshold(updatedProd.getLowStockThreshold());
        oldProd.setImagePath(updatedProd.getImagePath());
        oldProd.setDescription(updatedProd.getDescription());

        saveInventory();
        System.out.println("[Inventory] LOG: Product ID " + updatedProd.getID() + " updated successfully!");
    }

    public List<Product> getStorageList() {
        return storageList;
    }

    public String getFilePath() {
        return filePath;
    }

    private Product parseLine(String line) {
        try {
            String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            int id = Integer.parseInt(tokens[0].trim());
            String name = tokens[1].trim();
            Size size = Size.valueOf(tokens[2].trim().toUpperCase());
            Category category = Category.valueOf(tokens[3].trim().toUpperCase());
            double price = Double.parseDouble(tokens[4].trim());
            int stockQtd = Integer.parseInt(tokens[5].trim());

            int lowStockThreshold = Product.DEFAULT_LOW_STOCK;
            String imagePath;
            String description;

            if (tokens.length >= 9) {
                lowStockThreshold = Integer.parseInt(tokens[6].trim());
                imagePath = tokens[7].trim();
                description = tokens[8].trim().replace("\"", "");
            } else {
                imagePath = tokens[6].trim();
                description = tokens[7].trim().replace("\"", "");
            }

            return new Product(id, name, price, stockQtd, imagePath, size, category, description, lowStockThreshold);
        } catch (Exception e) {
            System.err.println("[Inventory] Ignoring invalid line [storage.csv]: " + line + " -> Error: " + e.getMessage());
            return null;
        }
    }

    public Product findProductById(int id) {
        for (Product p : storageList) {
            if (p.getID() == id) {
                return p;
            }
        }
        return null;
    }

    public boolean isWellFormatedProduct(Product p) {
        if (p == null) {
            System.err.println("[Inventory] VALIDATION FAIL: Product object is null.");
            return false;
        }
        if (p.getID() <= 0) {
            System.err.println("[Inventory] VALIDATION FAIL: ID must be greater than 0.");
            return false;
        }
        if (p.getName() == null || p.getName().trim().isEmpty()) {
            System.err.println("[Inventory] VALIDATION FAIL: Product name cannot be empty.");
            return false;
        }
        if (p.getSize() == null) {
            System.err.println("[Inventory] VALIDATION FAIL: Product size (Enum) cannot be null.");
            return false;
        }
        if (p.getCategory() == null) {
            System.err.println("[Inventory] VALIDATION FAIL: Product category (Enum) cannot be null.");
            return false;
        }
        if (p.getPrice() <= 0.0) {
            System.err.println("[Inventory] VALIDATION FAIL: Price must be greater than 0.");
            return false;
        }
        if (p.getStockQtd() < 0) {
            System.err.println("[Inventory] VALIDATION FAIL: Stock quantity cannot be negative.");
            return false;
        }
        if (p.getLowStockThreshold() < 0) {
            System.err.println("[Inventory] VALIDATION FAIL: Low stock threshold cannot be negative.");
            return false;
        }
        if (p.getImagePath() == null || p.getImagePath().trim().isEmpty()) {
            System.err.println("[Inventory] VALIDATION FAIL: Image path cannot be empty.");
            return false;
        }

        return true;
    }
}