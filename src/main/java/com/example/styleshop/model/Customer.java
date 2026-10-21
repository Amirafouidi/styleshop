package com.example.styleshop.model;

public abstract class Customer {
    private final String id;
    private final String name;
    private final String email;

    protected Customer(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Chaque type de client a ses propres avantages
    public abstract double getLoyaltyDiscountRate(); // Ex: 0.0 pour standard, 0.10 pour premium (10%)
    public abstract double getFreeShippingThreshold(); // Ex: 50.0€ pour standard, 20.0€ pour premium

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}