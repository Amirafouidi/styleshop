package com.example.styleshop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    private final String id;
    private final Customer customer;
    private final List<CartItem> items;
    private final LocalDate orderDate;
    private final double total;
    private OrderStatus status = OrderStatus.PENDING;

    public Order(String id, Customer customer, List<CartItem> items, double total) {
        this.id = id;
        this.customer = customer;
        // On copie la liste : si le panier est vidé ensuite, la commande garde ses lignes
        this.items = new ArrayList<>(items);
        this.orderDate = LocalDate.now();
        this.total = total;
    }

    public void markAsPaid() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Seule une commande en attente peut être payée.");
        }
        status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.PAID) {
            throw new IllegalStateException("Cette commande ne peut plus être annulée.");
        }
        status = OrderStatus.CANCELLED;
    }
    public void markAsReturned() {
        if (status != OrderStatus.PAID && status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Seule une commande payée ou expédiée peut être retournée.");
        }
        status = OrderStatus.RETURNED;
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
    public LocalDate getOrderDate() { return orderDate; }
    public double getTotal() { return total; }
    public OrderStatus getStatus() { return status; }
}
