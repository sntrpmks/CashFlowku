package com.example.cashflowkujava.models;

public class Product {
    private int id;
    private String name;
    private double price;
    private double modal; // Capital cost / Buy price
    private int stock;
    private String category;
    private String imagePath; // Product photo local path
    private String createdAt;

    public Product() {}

    public Product(int id, String name, double price, double modal, int stock, String category, String imagePath, String createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.modal = modal;
        this.stock = stock;
        this.category = category;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public Product(String name, double price, double modal, int stock, String category, String imagePath) {
        this.name = name;
        this.price = price;
        this.modal = modal;
        this.stock = stock;
        this.category = category;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getModal() {
        return modal;
    }

    public void setModal(double modal) {
        this.modal = modal;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name; // Useful for Spinner adapter
    }
}
