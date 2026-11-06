package com.example.styleshop.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.styleshop.exception.OutOfStockException;

public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Product product, Size size, int quantity) {
        // Si l'article dans cette même taille est déjà présent, on incrémente la quantité
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId()) && item.getSize() == size) {
                // On vérifie le total voulu (déjà au panier + ajout), pas seulement l'ajout
                checkStock(product, size, item.getQuantity() + quantity);
                item.addQuantity(quantity);
                return;
            }
        }
        // Sinon, on ajoute une nouvelle ligne au panier
        checkStock(product, size, quantity);
        items.add(new CartItem(product, size, quantity));
    }

    private void checkStock(Product product, Size size, int totalQuantity) {
        if (!product.hasStock(size, totalQuantity)) {
            throw new OutOfStockException("Stock insuffisant pour " + product.getName()
                    + " en taille " + size + " (disponible : " + product.getStock(size) + ")");
        }
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