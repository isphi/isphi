package com.budgetmanager.database.entity;

/**
 * Projection : total des montants d'un jour du mois, par type (revenu/dépense).
 */
public class DailyTotal {
    private int day;
    private String type;
    private double total;

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
