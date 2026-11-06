package com.example.styleshop.discount;

public class PercentageDiscount implements Discount {

    private final double rate;

    public PercentageDiscount(double rate) {
        // Un taux négatif augmenterait le prix, et un taux au-dessus de 1 le rendrait négatif.
        // 0 (aucune remise) et 1 (gratuit) sont acceptés : ce sont des cas limites testés.
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

