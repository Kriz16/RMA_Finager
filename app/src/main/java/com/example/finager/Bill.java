package com.example.finager;

public class Bill {
    private float amount;
    private String category;
    private String subcategory;
    //private long bill_id;
    private String date;
    private String userID;
    private int expense_or_income;

    public Bill() {
    }

    public Bill(float amount, String category, String subcategory, String date, String userID, int expense_or_income) {
        this.amount = amount;
        this.category = category;
        this.subcategory = subcategory;
        this.date = date;
        this.userID = userID;
        this.expense_or_income = expense_or_income;
        //this.bill_id = bill_id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getExpense_or_income() {
        return expense_or_income;
    }

    public void setExpense_or_income(int expense_or_income) {
        this.expense_or_income = expense_or_income;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

}
