package com.example.styleshop.service;

import com.example.styleshop.discount.Discount;
import com.example.styleshop.discount.PercentageDiscount;
import com.example.styleshop.exception.EmptyCartException;
import com.example.styleshop.model.Cart;
import com.example.styleshop.model.Customer;

public class PricingService {

    private final ShippingCalculator shippingCalculator;

    public PricingService(ShippingCalculator shippingCalculator) {
        this.shippingCalculator = shippingCalculator;
    }

    // Sans code promo : une remise de 0 % ne change rien au total
    public double calculateFinalTotal(Cart cart, Customer customer) {
        return calculateFinalTotal(cart, customer, new PercentageDiscount(0));
    }

    public double calculateFinalTotal(Cart cart, Customer customer, Discount promo) {
        if (cart.isEmpty()) {
            throw new EmptyCartException("Le panier est vide.");
        }
        double total = cart.calculateRawTotal();

        // 1. Remise de fidélité, qui dépend du type de client
        total = new PercentageDiscount(customer.getLoyaltyDiscountRate()).apply(total);

        // 2. Code promo
        total = promo.apply(total);

        // 3. Livraison, calculée sur le total après remises
        return total + shippingCalculator.calculate(customer, total);
    }
}