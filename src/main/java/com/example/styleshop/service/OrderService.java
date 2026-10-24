package com.example.styleshop.service;

import com.example.styleshop.discount.Discount;
import com.example.styleshop.discount.PercentageDiscount;
import com.example.styleshop.exception.OutOfStockException;
import com.example.styleshop.model.Cart;
import com.example.styleshop.model.CartItem;
import com.example.styleshop.model.Customer;
import com.example.styleshop.model.Order;

import java.util.UUID;

public class OrderService {

    private final PricingService pricingService;

    public OrderService(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    // Commande sans code promo
    public Order placeOrder(Cart cart, Customer customer) {
        return placeOrder(cart, customer, new PercentageDiscount(0));
    }

    public Order placeOrder(Cart cart, Customer customer, Discount promo) {
        // Le calcul vient en premier : il lance EmptyCartException si le panier est vide
        double total = pricingService.calculateFinalTotal(cart, customer, promo);

        // On vérifie TOUT le stock avant d'en retirer, sinon une erreur
        // sur le 3e article laisserait les 2 premiers déjà retirés
        for (CartItem item : cart.getItems()) {
            if (!item.getProduct().hasStock(item.getSize(), item.getQuantity())) {
                throw new OutOfStockException("Stock insuffisant pour "
                        + item.getProduct().getName() + " en taille " + item.getSize());
            }
        }

        for (CartItem item : cart.getItems()) {
            item.getProduct().reduceStock(item.getSize(), item.getQuantity());
        }

        Order order = new Order(UUID.randomUUID().toString(), customer, cart.getItems(), total);
        order.markAsPaid();
        cart.clear();
        return order;
    }

    public void cancelOrder(Order order) {
        // On change le statut d'abord : si l'annulation est interdite,
        // l'exception est lancée avant qu'on touche au stock
        order.cancel();
        for (CartItem item : order.getItems()) {
            item.getProduct().addStock(item.getSize(), item.getQuantity());
        }
    }
}