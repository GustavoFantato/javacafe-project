package service;

import model.enums.Size;
import model.Product;

import java.io.*;
import java.util.ArrayList; // To use the order's item list
import java.util.List;

public class InventoryService{

    // Attributes
    private List<Product> storageList;
    private final String filePath;

    // Constructor
    public InventoryService(String filePath){
        this.filePath = filePath;
        this.storageList= new ArrayList<>();
        loadInventory();
    }


    // Methods

    // Inventory manipulation methods

    // Loads all the CSV file in the inventory list
    public void loadInventory(){

        this.storageList.clear(); // clear the previous list

        try (BufferedReader reader = new BufferedReader(new FileReader(this.filePath))){
            String line;
            boolean isHeader = true;

            while((line = reader.readLine()) != null){
                if (isHeader){
                    isHeader = false; // Ignores the CSV's first line (header)
                    continue;
                }

                Product product = parseLine(line);
                if(product != null){
                    this.storageList.add(product);
                }
            }

        } catch(Exception e){
            System.err.println("An error occurred while reading storage file: " + e.getMessage());
        }

    }

    public void saveInventory(){

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.filePath))){

            // Write the header
            writer.write("id,name,size,price,stock,imagePath,description");
            writer.newLine(); // Jumps to next line

            for (Product p : storageList){
                String line = String.format("%d,%s,%s,%f,%d,%s,\"%s\"",
                        p.getID(),
                        p.getName(),
                        p.getSize().name(),
                        p.getPrice(),
                        p.getStockQtd(),
                        p.getImagePath(),
                        p.getDescription()
                );

                writer.write(line);
                writer.newLine();
            }
            System.out.printf("LOG: Storage saved successfully [%s]!\n",filePath);

        } catch (Exception e) {
            System.err.println("An error occurred while writing storage file: " + e.getMessage());
        }
    }

    // Getters
    public List<Product> getStorageList() {
        return storageList;
    }

    // Auxiliary method used in loadInventory()
    private Product parseLine(String line){
        try{
            String[] tokens = line.split(","); // split the line, using the common as split condition

            // Spliting and saving the data
            int ID = Integer.parseInt(tokens[0].trim());
            String name = tokens[1].trim();
            Size size = Size.valueOf(tokens[2].trim().toUpperCase());
            double price = Double.parseDouble(tokens[3].trim());
            int stockQtd = Integer.parseInt(tokens[4].trim());
            String imagePath = tokens[5].trim();
            String description = tokens[6].trim().replace("\"", "");

            // Associating to the object
            return new Product(ID, name, price, stockQtd, imagePath, size, description);
        } catch (Exception e){
            // If the line isn't well formated, the code ignores it and logs it on the console, but doesn't kill the program
            System.err.println("Ignoring invalid line [storage.csv]: " + line + " -> Error: " + e.getMessage());
            return null;
        }


    }





}