package com.example.styleshop.discount;

public class FixedAmountDiscount implements Discount {

    // ENCAPSULATION : attribut privé, on ne peut pas le modifier de l'extérieur.
    // final : le montant est fixé à la création et ne change plus.
    private final double amount;

    public FixedAmountDiscount(double amount) {
        // Validation dans le constructeur : on empêche de créer un objet incohérent
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
