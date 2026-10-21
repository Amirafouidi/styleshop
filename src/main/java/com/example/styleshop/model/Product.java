package com.example.styleshop.model;

import java.util.Collections;
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

    // Gestion du stock par taille
    public void addStock(Size size, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative.");
        }
        int currentStock = stockBySize.getOrDefault(size, 0);
        stockBySize.put(size, currentStock + quantity);
    }

    public boolean hasStock(Size size, int quantity) {
        return stockBySize.getOrDefault(size, 0) >= quantity;
    }

    public void reduceStock(Size size, int quantity) {
        if (!hasStock(size, quantity)) {
            throw new IllegalArgumentException("Stock insuffisant pour la taille " + size);
        }
        stockBySize.put(size, stockBySize.get(size) - quantity);
    }

    public int getStock(Size size) {
        return stockBySize.getOrDefault(size, 0);
    }

    public Map<Size, Integer> getStockBySize() {
        return Collections.unmodifiableMap(stockBySize);
    }

    // Getters de base
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
}