package com.example.styleshop.service;

import com.example.styleshop.exception.ReturnNotAllowedException;
import com.example.styleshop.exception.ReturnPeriodExpiredException;
import com.example.styleshop.model.CartItem;
import com.example.styleshop.model.Order;

import java.time.LocalDate;

public class ReturnService {

    public void processReturn(Order order) {
        processReturn(order, LocalDate.now());
    }

    // La date est un paramètre pour pouvoir tester le dernier jour autorisé
    public void processReturn(Order order, LocalDate today) {
        // On vérifie tous les articles avant de toucher au stock
        for (CartItem item : order.getItems()) {
            int periodDays = item.getProduct().getReturnPeriodDays();

            if (periodDays == 0) {
                throw new ReturnNotAllowedException(
                        item.getProduct().getName() + " ne peut pas être retourné.");
            }

            LocalDate lastDay = order.getOrderDate().plusDays(periodDays);
            if (today.isAfter(lastDay)) {
                throw new ReturnPeriodExpiredException(
                        "Le délai de retour de " + item.getProduct().getName() + " est dépassé.");
            }
        }

        order.markAsReturned();

        for (CartItem item : order.getItems()) {
            item.getProduct().addStock(item.getSize(), item.getQuantity());
        }
    }
}