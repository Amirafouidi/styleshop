package com.example.styleshop.service;

import com.example.styleshop.model.Customer;

public class ShippingCalculator {

    private static final double SHIPPING_FEE = 4.90;

    public double calculate(Customer customer, double total) {
        if (total >= customer.getFreeShippingThreshold()) {
            return 0.0;
        }
        return SHIPPING_FEE;
    }
}
