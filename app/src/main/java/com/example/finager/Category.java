package com.example.finager;

public class Category {
    private String userID;
    private String category;
    private String date;
    private int expense_or_income;
    private float total_amount;

    public Category() {
    }

    public Category(String userID, String category, String date, int expense_or_income, float total_amount) {
        this.userID = userID;
        this.category = category;
        this.date = date;
        this.expense_or_income = expense_or_income;
        this.total_amount = total_amount;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public float getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(float total_amount) {
        this.total_amount = total_amount;
    }
}
