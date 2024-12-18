package com.example.shared.models;

public class Income {
    private int Id;
    private int Amount;
    private String Date;
    private String TypeExpense;
    private String TypeIncome;
    private String Notes;

    public Income(int id, int amount, String date, String typeExpense, String typeIncome, String notes) {
        this.Id = id;
        this.Amount = amount;
        this.Date = date;
        this.TypeExpense = typeExpense;
        this.TypeIncome = typeIncome;
        this.Notes = notes;
    }
    //getters and setters
    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public int getAmount() {
        return Amount;
    }

    public void setAmount(int amount) {
        this.Amount = amount;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        this.Date = date;
    }

    public String getTypeExpense() {
        return TypeExpense;
    }

    public void setTypeExpense(String typeExpense) {
        this.TypeExpense = typeExpense;
    }

    public String getTypeIncome() {
        return TypeIncome;
    }

    public void setTypeIncome(String typeIncome) {
        this.TypeIncome = typeIncome;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        this.Notes = notes;
    }

    public String toString() {
        return  "id =" + Id +
                ", Amount ='" + Amount + '\'' +
                ", Date ='" + Date + '\'' +
                ", Expense =" + TypeExpense + '\'' +
                ", Income =" + TypeIncome+ '\'' +
                ", Note =" + Notes;

    }

}
