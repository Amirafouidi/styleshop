package com.example.styleshop.discount;

public class FixedAmountDiscount implements Discount {

    private final double amount;

    public FixedAmountDiscount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Le montant ne peut pas être négatif.");
        }
        this.amount = amount;
    }

    @Override
    public double apply(double total) {
        // On ne descend jamais sous 0 : une remise ne peut pas rendre de l'argent au client
        return Math.max(0, total - amount);
    }
}
