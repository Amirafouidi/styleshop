package com.example.styleshop.model;

public class Accessory extends Product{
    public Accessory(String id, String name, double price) {
        super(id, name, price);
    }

    @Override
    public int getReturnPeriodDays() {
        return 0;
    }
}
