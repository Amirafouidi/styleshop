package com.example.styleshop.model;

public class Shoes extends Product{
    public Shoes(String id, String name, double price) {
        super(id, name, price);
    }

    @Override
    public int getReturnPeriodDays() {
        return 30;
    }
}
