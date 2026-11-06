package com.example.styleshop.discount;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixedAmountDiscountTest {

    // CAS NORMAL
    @Test
    void shouldSubtractFixedAmountFromTotal() {
        Discount discount = new FixedAmountDiscount(5);
        assertEquals(15.0, discount.apply(20.0), 0.001);
    }

    // CAS LIMITES : remise nulle, et remise supérieure au total (le total ne devient jamais négatif)
    @Test
    void shouldKeepSameTotalWhenAmountIsZero() {
        assertEquals(20.0, new FixedAmountDiscount(0).apply(20.0), 0.001);
    }

    @Test
    void shouldNeverGoBelowZero() {
        assertEquals(0.0, new FixedAmountDiscount(50).apply(20.0), 0.001);
    }

    // CAS INVALIDE
    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> new FixedAmountDiscount(-1));
    }
}