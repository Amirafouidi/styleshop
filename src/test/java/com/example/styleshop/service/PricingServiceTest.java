package com.example.styleshop.service;

import com.example.styleshop.discount.PercentageDiscount;
import com.example.styleshop.exception.EmptyCartException;
import com.example.styleshop.model.Cart;
import com.example.styleshop.model.Clothing;
import com.example.styleshop.model.Customer;
import com.example.styleshop.model.PremiumCustomer;
import com.example.styleshop.model.Product;
import com.example.styleshop.model.Size;
import com.example.styleshop.model.StandardCustomer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricingServiceTest {

    // Les frais de livraison sont de 4,90 €. Seuil de livraison offerte :
    // 50 € pour un client standard, 20 € pour un client premium (10 % de remise de fidélité).
    private final PricingService pricingService = new PricingService(new ShippingCalculator());
    private final Customer standard = new StandardCustomer("U1", "Alice", "alice@mail.com");
    private final Customer premium = new PremiumCustomer("U2", "Bob", "bob@mail.com");

    // Méthode d'aide : construit un panier avec un seul produit, pour garder les tests lisibles
    private Cart cartWith(double price, int quantity) {
        Product product = new Clothing("C1", "T-shirt", price);
        product.addStock(Size.M, 100);
        Cart cart = new Cart();
        cart.addItem(product, Size.M, quantity);
        return cart;
    }

    // CAS NORMAUX
    @Test
    void shouldAddShippingFeeWhenBelowThreshold() {
        // 20 € < 50 € : 20 + 4,90 de livraison
        assertEquals(24.90, pricingService.calculateFinalTotal(cartWith(20.0, 1), standard), 0.001);
    }

    @Test
    void shouldApplyLoyaltyDiscountForPremiumCustomer() {
        // 2 x 20 = 40, moins 10 % = 36, et 36 >= 20 donc livraison offerte
        assertEquals(36.0, pricingService.calculateFinalTotal(cartWith(20.0, 2), premium), 0.001);
    }

    @Test
    void shouldApplyPromoCode() {
        // 3 x 20 = 60, moins 20 % = 48, et 48 < 50 donc 48 + 4,90
        double total = pricingService.calculateFinalTotal(
                cartWith(20.0, 3), standard, new PercentageDiscount(0.20));
        assertEquals(52.90, total, 0.001);
    }

    // CAS LIMITES
    @Test
    void shouldOfferFreeShippingExactlyAtThreshold() {
        // 2 x 25 = 50 pile : le seuil est atteint, donc pas de livraison
        assertEquals(50.0, pricingService.calculateFinalTotal(cartWith(25.0, 2), standard), 0.001);
    }

    @Test
    void shouldChargeShippingJustBelowThreshold() {
        // 2 x 24,99 = 49,98 : un centime de moins que le seuil, donc livraison payante
        assertEquals(54.88, pricingService.calculateFinalTotal(cartWith(24.99, 2), standard), 0.001);
    }

    @Test
    void shouldStillChargeShippingWhenPromoMakesTotalZero() {
        // Remise de 100 % : le produit est gratuit, mais la livraison reste due (règle choisie)
        double total = pricingService.calculateFinalTotal(
                cartWith(20.0, 1), standard, new PercentageDiscount(1));
        assertEquals(4.90, total, 0.001);
    }

    // CAS INVALIDE
    @Test
    void shouldThrowWhenCartIsEmpty() {
        assertThrows(EmptyCartException.class,
                () -> pricingService.calculateFinalTotal(new Cart(), standard));
    }
}