package com.example.electricitybillapp;

public class Bill {
    private long id;
    private String month;
    private int units;
    private double rebate;
    private double total;
    private double finalAmount;

    public Bill(long id, String month, int units, double rebate, double total, double finalAmount) {
        this.id = id;
        this.month = month;
        this.units = units;
        this.rebate = rebate;
        this.total = total;
        this.finalAmount = finalAmount;
    }

    // Getters
    public long getId() { return id; }
    public String getMonth() { return month; }
    public int getUnits() { return units; }
    public double getRebate() { return rebate; }
    public double getTotal() { return total; }
    public double getFinalAmount() { return finalAmount; }

    // Optional setters if you need them
    public void setId(long id) { this.id = id; }
    public void setMonth(String month) { this.month = month; }
    public void setUnits(int units) { this.units = units; }
    public void setRebate(double rebate) { this.rebate = rebate; }
    public void setTotal(double total) { this.total = total; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    // Optional toString() for debugging
    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", month='" + month + '\'' +
                ", units=" + units +
                ", rebate=" + rebate +
                ", total=" + total +
                ", finalAmount=" + finalAmount +
                '}';
    }
}