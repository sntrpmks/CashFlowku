package com.example.cashflowkujava.models;

public class Expense {
    private int id;
    private String category;
    private double amount;
    private String date; // Format: YYYY-MM-DD
    private String notes;
    private String receiptPhoto; // URI or file path to receipt photo
    private String createdAt;

    public Expense() {}

    public Expense(int id, String category, double amount, String date, String notes, String receiptPhoto, String createdAt) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.receiptPhoto = receiptPhoto;
        this.createdAt = createdAt;
    }

    public Expense(String category, double amount, String date, String notes, String receiptPhoto) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.receiptPhoto = receiptPhoto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReceiptPhoto() {
        return receiptPhoto;
    }

    public void setReceiptPhoto(String receiptPhoto) {
        this.receiptPhoto = receiptPhoto;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
