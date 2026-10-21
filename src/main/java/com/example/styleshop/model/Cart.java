package com.example.styleshop.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Product product, Size size, int quantity) {
        // Si l'article dans cette même taille est déjà présent, on incrémente la quantité
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId()) && item.getSize() == size) {
                item.addQuantity(quantity);
                return;
            }
        }
        // Sinon, on ajoute une nouvelle ligne au panier
        items.add(new CartItem(product, size, quantity));
    }

    public void removeItem(String productId, Size size) {
        items.removeIf(item -> item.getProduct().getId().equals(productId) && item.getSize() == size);
    }

    public void clear() {
        items.clear();
    }

    public double calculateRawTotal() {
        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}