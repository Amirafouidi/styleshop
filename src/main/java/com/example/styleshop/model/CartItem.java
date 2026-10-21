package com.example.styleshop.model;

public class CartItem {
    private final Product product;
    private final Size size;
    private int quantity;

    public CartItem(Product product, Size size, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La quantité doit être supérieure à zéro.");
        }
        this.product = product;
        this.size = size;
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("L'ajout de quantité doit être positif.");
        }
        this.quantity += amount;
    }

    public Product getProduct() {
        return product;
    }

    public Size getSize() {
        return size;
    }

    public int getQuantity() {
        return quantity;
    }
}
