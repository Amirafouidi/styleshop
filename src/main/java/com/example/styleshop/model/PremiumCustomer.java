package com.example.styleshop.model;

public class PremiumCustomer extends Customer {

    public PremiumCustomer(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public double getLoyaltyDiscountRate() {
        return 0.10; // 10% de réduction permanente
    }

    @Override
    public double getFreeShippingThreshold() {
        return 20.0; // Livraison offerte dès 20€ d'achats
    }
}
