package com.example.shared.models;

public class Transaction {
    private long Id;
    private String Type;
    private String Category;
    private int Amount;
    private String Date;
    private String Note;

    public Transaction(String type, String category, int amount, String date, String note) {
        Type = type;
        Category = category;
        Amount = amount;
        Date = date;
        Note = note;
    }

    public Transaction(long id, String type, String category, int amount, String date, String note) {
        Id = id;
        Type = type;
        Category = category;
        Amount = amount;
        Date = date;
        Note = note;
    }
    //getters and setters
    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public int getAmount() {
        return Amount;
    }

    public void setAmount(int amount) {
        Amount = amount;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getNote() {
        return Note;
    }

    public void setNote(String note) {
        Note = note;
    }

}
