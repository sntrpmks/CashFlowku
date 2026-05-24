package com.example.cashflowkujava.models;

public class Sale {
    private int id;
    private String date; // Format: YYYY-MM-DD
    private String productName;
    private int qty;
    private double price;
    private double modal; // Modal cost of the product when sold
    private double subtotal;
    private String paymentMethod;
    private String notes;
    private String createdAt;
    private String productImagePath;

    public Sale() {}

    public Sale(int id, String date, String productName, int qty, double price, double modal, double subtotal, String paymentMethod, String notes, String createdAt) {
        this.id = id;
        this.date = date;
        this.productName = productName;
        this.qty = qty;
        this.price = price;
        this.modal = modal;
        this.subtotal = subtotal;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Sale(String date, String productName, int qty, double price, double modal, double subtotal, String paymentMethod, String notes) {
        this.date = date;
        this.productName = productName;
        this.qty = qty;
        this.price = price;
        this.modal = modal;
        this.subtotal = subtotal;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
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

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getProductImagePath() {
        return productImagePath;
    }

    public void setProductImagePath(String productImagePath) {
        this.productImagePath = productImagePath;
    }
}
