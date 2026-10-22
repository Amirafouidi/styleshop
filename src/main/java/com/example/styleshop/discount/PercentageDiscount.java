package com.example.styleshop.discount;

public class PercentageDiscount implements Discount {

    private final double rate;

    public PercentageDiscount(double rate) {
        if (rate < 0 || rate > 1) {
            throw new IllegalArgumentException("Le taux doit être entre 0 et 1.");
        }
        this.rate = rate;
    }

    @Override
    public double apply(double total) {
        return total * (1 - rate);
    }
}

