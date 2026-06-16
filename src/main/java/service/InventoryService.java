package service;

import model.enums.Size;
import model.enums.Category;
import model.Product;
import service.enums.FilterOperator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {

    // Attributes
    private List<Product> storageList;
    private final String filePath;

    // Constructor
    public InventoryService(String filePath) {
        this.filePath = filePath;
        this.storageList = new ArrayList<>();
        loadInventory();
    }

    // Methods

    // CSV Archive Manipulation Methods
    public void loadInventory() {
        this.storageList.clear(); // clear the previous list

        try (BufferedReader reader = new BufferedReader(new FileReader(this.filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Ignores the CSV's first line (header)
                    continue;
                }

                Product product = parseLine(line);
                if (product != null) {
                    this.storageList.add(product);
                }
            }

        } catch (Exception e) {
            System.err.println("An error occurred while reading storage file: " + e.getMessage());
        }
    }

    public void saveInventory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath))) {


            writer.write("id,name,size,category,price,stock,imagePath,description");
            writer.newLine();

            for (Product p : storageList) {
                String line = String.format(java.util.Locale.US, "%d,%s,%s,%s,%.2f,%d,%s,\"%s\"",
                        p.getID(),
                        p.getName(),
                        p.getSize().name(),
                        p.getCategory().name(),
                        p.getPrice(),
                        p.getStockQtd(),
                        p.getImagePath(),
                        p.getDescription()
                );

                writer.write(line);
                writer.newLine();
            }
            System.out.printf("LOG: Storage saved successfully [%s]!\n", filePath);

        } catch (Exception e) {
            System.err.println("An error occurred while writing storage file: " + e.getMessage());
        }
    }

    // Stock manipulation methods
    public void decreaseProductStock(int id, int qtd) throws exception.OutOfStockException {
        Product p = findProductById(id);

        if (p != null) {
            p.decreaseStock(qtd);
            saveInventory();
        } else {
            System.err.println("FAIL: Product with ID " + id + " not found!");
        }
    }

    public void increaseProductStock(int id, int qtd) {
        Product p = findProductById(id);

        if (p != null) {
            p.increaseStock(qtd);
            saveInventory();
        } else {
            System.err.println("FAIL: Product with ID " + id + " not found!");
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

    // Adding/Removing/Updating products in DataBase
    public void addProductStorage(Product newProd) {
        if (isWellFormatedProduct(newProd)) {
            storageList.add(newProd);
            saveInventory();
            System.out.println("LOG: Product added successfully!");
        } else {
            System.out.println("FAIL: Bad formatted product!");
        }
    }

    public void removeProductStorage(int id) {
        Product p = findProductById(id);

        if (p != null) {
            storageList.remove(p);
            saveInventory();
            System.out.println("LOG: Product removed successfully!");
        } else {
            System.out.println("FAIL: Product not found!");
        }
    }

    public void updateProductStorage(Product updatedProd) {
        if (!isWellFormatedProduct(updatedProd)) {
            System.err.println("FAIL: Cannot update. Product contains formatting errors!");
            return;
        }

        Product oldProd = findProductById(updatedProd.getID());

        if (oldProd == null) {
            System.err.println("FAIL: Cannot update. Product ID " + updatedProd.getID() + " not found. ID alteration is not allowed!");
            return;
        }

        oldProd.setName(updatedProd.getName());
        oldProd.setSize(updatedProd.getSize());
        oldProd.setCategory(updatedProd.getCategory());
        oldProd.setPrice(updatedProd.getPrice());
        oldProd.setStockQtd(updatedProd.getStockQtd());
        oldProd.setImagePath(updatedProd.getImagePath());
        oldProd.setDescription(updatedProd.getDescription());

        saveInventory();
        System.out.println("LOG: Product ID " + updatedProd.getID() + " updated successfully!");
    }

    // Getters
    public List<Product> getStorageList() { return storageList; }
    public String getFilePath() { return filePath; }

    // Auxiliary method used in loadInventory()
    private Product parseLine(String line) {
        try {
            String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");


            int ID = Integer.parseInt(tokens[0].trim());
            String name = tokens[1].trim();
            Size size = Size.valueOf(tokens[2].trim().toUpperCase());
            Category category = Category.valueOf(tokens[3].trim().toUpperCase());
            double price = Double.parseDouble(tokens[4].trim());
            int stockQtd = Integer.parseInt(tokens[5].trim());
            String imagePath = tokens[6].trim();
            String description = tokens[7].trim().replace("\"", "");

            return new Product(ID, name, price, stockQtd, imagePath, size, category, description);
        } catch (Exception e) {
            System.err.println("Ignoring invalid line [storage.csv]: " + line + " -> Error: " + e.getMessage());
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
            System.err.println("VALIDATION FAIL: Product object is null.");
            return false;
        }
        if (p.getID() <= 0) {
            System.err.println("VALIDATION FAIL: ID must be greater than 0.");
            return false;
        }
        if (p.getName() == null || p.getName().trim().isEmpty()) {
            System.err.println("VALIDATION FAIL: Product name cannot be empty.");
            return false;
        }
        if (p.getSize() == null) {
            System.err.println("VALIDATION FAIL: Product size (Enum) cannot be null.");
            return false;
        }
        if (p.getCategory() == null) {
            System.err.println("VALIDATION FAIL: Product category (Enum) cannot be null.");
            return false;
        }
        if (p.getPrice() <= 0.0) {
            System.err.println("VALIDATION FAIL: Price must be greater than 0.");
            return false;
        }
        if (p.getStockQtd() < 0) {
            System.err.println("VALIDATION FAIL: Stock quantity cannot be negative.");
            return false;
        }
        if (p.getImagePath() == null || p.getImagePath().trim().isEmpty()) {
            System.err.println("VALIDATION FAIL: Image path cannot be empty.");
            return false;
        }

        return true;
    }
}