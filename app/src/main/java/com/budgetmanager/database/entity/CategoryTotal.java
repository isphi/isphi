package com.budgetmanager.database.entity;

public class CategoryTotal {
    private String categoryName;
    private String type;
    private double total;

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
