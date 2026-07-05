package com.budgetmanager.ui.calendar;

/**
 * Représente une cellule du calendrier mensuel.
 * Un jour à 0 correspond à une cellule vide (avant le 1er du mois).
 */
public class CalendarDay {

    private final int day;
    private final boolean weekend;
    private final boolean holiday;
    private final String holidayName;
    private double revenue;
    private double expense;

    public CalendarDay(int day, boolean weekend, boolean holiday, String holidayName) {
        this.day = day;
        this.weekend = weekend;
        this.holiday = holiday;
        this.holidayName = holidayName;
    }

    public int getDay() { return day; }
    public boolean isEmpty() { return day == 0; }
    public boolean isWeekend() { return weekend; }
    public boolean isHoliday() { return holiday; }
    public String getHolidayName() { return holidayName; }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

    public double getExpense() { return expense; }
    public void setExpense(double expense) { this.expense = expense; }

    public boolean hasTransactions() { return revenue > 0 || expense > 0; }
}
