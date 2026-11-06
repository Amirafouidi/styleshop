package com.example.styleshop;

import com.example.styleshop.exception.InvalidProductDataException;
import com.example.styleshop.model.Customer;
import com.example.styleshop.model.PremiumCustomer;
import com.example.styleshop.model.Product;
import com.example.styleshop.model.StandardCustomer;
import com.example.styleshop.service.OrderService;
import com.example.styleshop.service.PricingService;
import com.example.styleshop.service.ReturnService;
import com.example.styleshop.service.ShippingCalculator;
import com.example.styleshop.ui.ConsoleMenu;
import com.example.styleshop.utils.CsvProductLoader;

import java.util.List;

public class Application {

    public static void main(String[] args) {
        //CHARGEMENT DES DONNÉES depuis le fichier products.csv
        CsvProductLoader loader = new CsvProductLoader();
        List<Product> catalog;
        try {
            catalog = loader.loadFromResource("products.csv");
        } catch (InvalidProductDataException e) {
            // Il ne reste ici que le cas grave : le fichier est introuvable
            System.out.println("Impossible de charger le catalogue : " + e.getMessage());
            return;
        }
        // Les lignes invalides ont été ignorées : on les signale sans arrêter le programme
        loader.getErrors().forEach(error -> System.out.println("Avertissement : " + error));

        // Assemblage : chaque service reçoit ce dont il a besoin
        PricingService pricingService = new PricingService(new ShippingCalculator());
        OrderService orderService = new OrderService(pricingService);
        ReturnService returnService = new ReturnService();

        List<Customer> customers = List.of(
                new StandardCustomer("U1", "Alice Martin", "alice@mail.com"),
                new PremiumCustomer("U2", "Bob Durand", "bob@mail.com"));

        new ConsoleMenu(catalog, customers, orderService, returnService).run();
    }
}