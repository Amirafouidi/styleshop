package com.example.styleshop.discount;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PercentageDiscountTest {

    // CAS NORMAL
    @Test
    void shouldApplyTwentyPercentDiscount() {
        Discount discount = new PercentageDiscount(0.20);
        assertEquals(80.0, discount.apply(100.0), 0.001);
    }

    // CAS LIMITES : 0 % ne change rien, 100 % rend le total gratuit
    @Test
    void shouldKeepSameTotalWhenRateIsZero() {
        assertEquals(50.0, new PercentageDiscount(0).apply(50.0), 0.001);
    }

    @Test
    void shouldReturnZeroWhenRateIsOneHundredPercent() {
        assertEquals(0.0, new PercentageDiscount(1).apply(50.0), 0.001);
    }

    // CAS INVALIDES : un taux hors de 0 à 1 est refusé
    @Test
    void shouldRejectNegativeRate() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(-0.1));
    }

    @Test
    void shouldRejectRateAboveOne() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(1.5));
    }
}