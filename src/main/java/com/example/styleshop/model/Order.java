package com.example.styleshop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    // ENCAPSULATION : tout est privé et final sauf le statut, seule donnée qui évolue

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

    // CYCLE DE VIE : chaque méthode ci-dessous n'est autorisée qu'à partir de certains statuts.
    // PENDING -> PAID -> SHIPPED -> RETURNED, avec CANCELLED possible avant l'expédition.
    // Sinon on lève une IllegalStateException : l'objet protège lui-même sa cohérence.
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
    public void markAsShipped() {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Seule une commande payée peut être expédiée.");
        }
        status = OrderStatus.SHIPPED;
    }

    public void markAsReturned() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Seule une commande expédiée peut être retournée.");
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
