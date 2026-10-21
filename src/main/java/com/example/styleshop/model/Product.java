package com.example.styleshop.model;

import java.util.EnumMap;
import java.util.Map;

public abstract class Product {
    private final String id;
    private final String name;
    private final double price;
    private final Map<Size, Integer> stockBySize = new EnumMap<>(Size.class);

    protected Product(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // Chaque type de produit a sa propre règle de retour
    public abstract int getReturnPeriodDays();

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}
