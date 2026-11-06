package com.example.styleshop.ui;

import com.example.styleshop.discount.Discount;
import com.example.styleshop.discount.FixedAmountDiscount;
import com.example.styleshop.discount.PercentageDiscount;
import com.example.styleshop.exception.EmptyCartException;
import com.example.styleshop.exception.OutOfStockException;
import com.example.styleshop.exception.ReturnNotAllowedException;
import com.example.styleshop.exception.ReturnPeriodExpiredException;
import com.example.styleshop.model.Cart;
import com.example.styleshop.model.CartItem;
import com.example.styleshop.model.Customer;
import com.example.styleshop.model.Order;
import com.example.styleshop.model.OrderStatus;
import com.example.styleshop.model.Product;
import com.example.styleshop.model.Size;
import com.example.styleshop.service.OrderService;
import com.example.styleshop.service.ReturnService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

// cette classe ne s'occupe que de l'interaction avec l'utilisateur (affichage + saisie).
// Toute la logique métier (prix, stock, retours) est déléguée aux services.
public class ConsoleMenu {

    private final List<Product> catalog;
    private final List<Customer> customers;
    // Les services sont reçus par le constructeur :
    // le menu ne les crée pas lui-même, c'est Application qui les assemble.
    private final OrderService orderService;
    private final ReturnService returnService;
    private final Cart cart = new Cart();
    private final List<Order> orders = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private Customer customer;

    public ConsoleMenu(List<Product> catalog, List<Customer> customers,
                       OrderService orderService, ReturnService returnService) {
        this.catalog = catalog;
        this.customers = customers;
        this.orderService = orderService;
        this.returnService = returnService;
    }

    public void run() {
        chooseCustomer();
        boolean running = true;
        while (running) {
            printMenu();
            try {
                int choice = readInt("Votre choix : ");
                // Switch moderne : plus court, pas de break à oublier
                switch (choice) {
                    case 1 -> showCatalog();
                    case 2 -> addToCart();
                    case 3 -> showCart();
                    case 4 -> placeOrder();
                    case 5 -> payOrder();
                    case 6 -> trackOrders();
                    case 7 -> cancelOrder();
                    case 8 -> returnOrder();
                    case 0 -> running = false;
                    default -> System.out.println("Choix inconnu.");
                }
                // GESTION DES EXCEPTIONS : les erreurs métier
                // sont attrapées ici et affichées, le programme ne plante pas et la boucle continue
            } catch (OutOfStockException | EmptyCartException
                     | ReturnNotAllowedException | ReturnPeriodExpiredException e) {
                //  l'action est refusée, le programme continue
                System.out.println("Opération refusée : " + e.getMessage());
                // saisie invalide (IllegalArgument) ou action interdite
                // dans l'état actuel de la commande (IllegalState)
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Action impossible : " + e.getMessage());
            }
        }
        System.out.println("Au revoir !");
    }

    private void chooseCustomer() {
        System.out.println("=== StyleShop ===");
        System.out.println("Choisissez un client :");
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            // La liste contient des Customer : le vrai type (Standard ou Premium) est connu à l'exécution
            System.out.println((i + 1) + ". " + c.getName() + " (" + c.getClass().getSimpleName() + ")");
        }
        // On redemande tant qu'aucun client valide n'est choisi
        while (customer == null) {
            try {
                int choice = readInt("Votre choix : ");
                if (choice >= 1 && choice <= customers.size()) {
                    customer = customers.get(choice - 1);
                } else {
                    System.out.println("Choix invalide.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Bienvenue " + customer.getName() + " !");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Voir le catalogue");
        System.out.println("2. Ajouter un article au panier");
        System.out.println("3. Voir le panier");
        System.out.println("4. Valider le panier (crée la commande)");
        System.out.println("5. Payer une commande");
        System.out.println("6. Suivre mes commandes");
        System.out.println("7. Annuler une commande");
        System.out.println("8. Retourner une commande");
        System.out.println("0. Quitter");
    }

    private void showCatalog() {
        // POLYMORPHISME : la liste contient des Product, qu'ils soient Clothing, Shoes ou Accessory
        for (Product p : catalog) {
            System.out.printf("%s - %s - %.2f € - stock : %s%n",
                    p.getId(), p.getName(), p.getPrice(), p.getStockBySize());
        }
    }

    private void addToCart() {
        String id = readLine("Id du produit : ");
        // Optional : findProduct peut ne rien trouver, orElseThrow évite de manipuler null
        Product product = findProduct(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit inconnu : " + id));
        Size size = Size.valueOf(readLine("Taille (XS, S, M, L, XL, ONE_SIZE) : ").toUpperCase());
        int quantity = readInt("Quantité : ");
        // Le menu ne vérifie pas le stock : c'est Cart qui le fait et lance OutOfStockException
        cart.addItem(product, size, quantity);
        System.out.println("Ajouté au panier.");
    }

    private void showCart() {
        if (cart.isEmpty()) {
            System.out.println("Le panier est vide.");
            return;
        }
        for (CartItem item : cart.getItems()) {
            System.out.printf("%s (%s) x%d = %.2f €%n",
                    item.getProduct().getName(), item.getSize(), item.getQuantity(), item.getSubtotal());
        }
        System.out.printf("Total avant remises : %.2f €%n", cart.calculateRawTotal());
    }

    private void placeOrder() {
        Discount promo = askPromo();
        // Tout le calcul (remises, livraison, stock) est fait par le service, pas ici
        Order order = orderService.placeOrder(cart, customer, promo);
        orders.add(order);
        System.out.printf("Commande %s enregistrée. Total à payer : %.2f €.%n",
                shortId(order), order.getTotal());
    }

    private void cancelOrder() {
        Order order = selectOrder();
        orderService.cancelOrder(order);
        System.out.println("Commande " + shortId(order) + " annulée, le stock est restitué.");
    }

    private void returnOrder() {
        Order order = selectOrder();
        returnService.processReturn(order);
        System.out.println("Commande " + shortId(order) + " retournée, le stock est remis à jour.");
    }

    private void payOrder() {
        Order order = selectOrder();
        orderService.payOrder(order);
        // Simplification : la boutique expédie la commande dès qu'elle est payée
        orderService.shipOrder(order);
        System.out.println("Paiement de la commande " + shortId(order)
                + " accepté, commande expédiée (simulation).");
    }

    private void trackOrders() {
        if (orders.isEmpty()) {
            System.out.println("Aucune commande pour le moment.");
            return;
        }
        for (Order o : orders) {
            System.out.printf("%s - %s - %.2f € - %s%n",
                    shortId(o), o.getOrderDate(), o.getTotal(), statusLabel(o.getStatus()));
        }
    }

    // Le texte affiché est une affaire d'interface, donc il est ici et pas dans l'enum OrderStatus.
    // Le switch sur une enum sans default : le compilateur nous prévient si on ajoute un statut
    private String statusLabel(OrderStatus status) {
        return switch (status) {
            case PENDING -> "En attente de paiement";
            case PAID -> "Payée, en préparation";
            case SHIPPED -> "Expédiée";
            case CANCELLED -> "Annulée";
            case RETURNED -> "Retournée";
        };
    }

    // la méthode renvoie un Discount, sans que l'appelant sache
    // si c'est un pourcentage ou un montant fixe. "Pas de code" = remise de 0 %.
    private Discount askPromo() {
        String code = readLine("Code promo (Entrée pour aucun) : ").trim().toUpperCase();
        return switch (code) {
            case "" -> new PercentageDiscount(0);
            case "SOLDES20" -> new PercentageDiscount(0.20);
            case "MOINS5" -> new FixedAmountDiscount(5);
            default -> throw new IllegalArgumentException("Code promo inconnu : " + code);
        };
    }

    private Order selectOrder() {
        if (orders.isEmpty()) {
            throw new IllegalStateException("Aucune commande pour le moment.");
        }
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            System.out.printf("%d. %s - %.2f € - %s%n", i + 1, shortId(o), o.getTotal(), o.getStatus());
        }
        int choice = readInt("Numéro dans la liste (1, 2...) : ");
        if (choice < 1 || choice > orders.size()) {
            throw new IllegalArgumentException("Numéro invalide.");
        }
        return orders.get(choice - 1);
    }

    // STREAM + OPTIONAL : on filtre le catalogue pour trouver le produit,
    // findFirst renvoie un Optional vide s'il n'y en a aucun
    private Optional<Product> findProduct(String id) {
        return catalog.stream()
                .filter(p -> p.getId().equalsIgnoreCase(id.trim()))
                .findFirst();
    }

    // Les 8 premiers caractères suffisent pour s'y retrouver à l'écran
    private String shortId(Order order) {
        return order.getId().substring(0, 8);
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    // On transforme l'erreur technique de Java (NumberFormatException) en message lisible.
    // elle est ensuite attrapée dans run() comme IllegalArgumentException.
    private int readInt(String prompt) {
        try {
            return Integer.parseInt(readLine(prompt).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Veuillez entrer un nombre.");
        }
    }
}