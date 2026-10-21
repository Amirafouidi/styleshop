package com.example.styleshop.model;

public class Clothing extends Product{
    public Clothing(String id, String name, double price) {
        super(id, name, price);
    }

    @Override
    public int getReturnPeriodDays() {
        return 14;
    }
}
