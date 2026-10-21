package com.example.styleshop.model;

public class StandardCustomer extends Customer {

    public StandardCustomer(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public double getLoyaltyDiscountRate() {
        return 0.0; // Pas de réduction de fidélité automatique
    }

    @Override
    public double getFreeShippingThreshold() {
        return 50.0; // Livraison offerte à partir de 50€ d'achats
    }
}
