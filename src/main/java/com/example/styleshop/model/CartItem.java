package com.example.styleshop.model;

public class CartItem {
    private final Product product;
    private final Size size;
    private int quantity;

    public CartItem(Product product, Size size, int quantity) {
        // Validation dans le constructeur : impossible de créer une ligne à 0 ou négative
        if (quantity <= 0) {
            throw new IllegalArgumentException("La quantité doit être supérieure à zéro.");
        }
        this.product = product;
        this.size = size;
        this.quantity = quantity;
    }
    // le sous-total est calculé à la demande, jamais stocké,
    // pour ne pas risquer qu'il soit différent du prix et de la quantité
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    // On passe par une méthode plutôt qu'un setter : la quantité ne peut qu'augmenter,
    // et seulement d'une valeur positive
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
