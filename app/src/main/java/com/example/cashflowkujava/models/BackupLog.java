package com.example.cashflowkujava.models;

public class BackupLog {
    private int id;
    private String filename;
    private String filepath;
    private long size;
    private String date; // YYYY-MM-DD HH:MM:SS
    private String status; // SUCCESS or FAILED

    public BackupLog() {}

    public BackupLog(int id, String filename, String filepath, long size, String date, String status) {
        this.id = id;
        this.filename = filename;
        this.filepath = filepath;
        this.size = size;
        this.date = date;
        this.status = status;
    }

    public BackupLog(String filename, String filepath, long size, String date, String status) {
        this.filename = filename;
        this.filepath = filepath;
        this.size = size;
        this.date = date;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
