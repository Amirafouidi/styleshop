package com.example.styleshop.service;

import com.example.styleshop.exception.ReturnNotAllowedException;
import com.example.styleshop.exception.ReturnPeriodExpiredException;
import com.example.styleshop.model.Accessory;
import com.example.styleshop.model.Cart;
import com.example.styleshop.model.Clothing;
import com.example.styleshop.model.Customer;
import com.example.styleshop.model.Order;
import com.example.styleshop.model.OrderStatus;
import com.example.styleshop.model.Product;
import com.example.styleshop.model.Shoes;
import com.example.styleshop.model.Size;
import com.example.styleshop.model.StandardCustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReturnServiceTest {

    private final OrderService orderService =
            new OrderService(new PricingService(new ShippingCalculator()));
    private final ReturnService returnService = new ReturnService();
    private final Customer customer = new StandardCustomer("U1", "Alice", "alice@mail.com");

    // Retour : 14 jours pour un vêtement, 30 pour des chaussures, impossible pour un accessoire
    private final Product tshirt = new Clothing("C1", "T-shirt", 20.0);
    private final Product shoes = new Shoes("S1", "Baskets", 60.0);
    private final Product cap = new Accessory("A1", "Casquette", 15.0);

    @BeforeEach
    void setUp() {
        tshirt.addStock(Size.M, 10);
        shoes.addStock(Size.M, 5);
        cap.addStock(Size.ONE_SIZE, 20);
    }

    // Méthode d'aide : une commande avec un seul article, déjà payée et expédiée
    // (un retour n'est possible que sur une commande expédiée)
    private Order shippedOrder(Product product, Size size, int quantity) {
        Cart cart = new Cart();
        cart.addItem(product, size, quantity);
        Order order = orderService.placeOrder(cart, customer);
        orderService.payOrder(order);
        orderService.shipOrder(order);
        return order;
    }

    // CAS NORMAUX
    @Test
    void shouldReturnOrderAndRestoreStock() {
        Order order = shippedOrder(tshirt, Size.M, 3);
        assertEquals(7, tshirt.getStock(Size.M));

        returnService.processReturn(order);

        assertEquals(OrderStatus.RETURNED, order.getStatus());
        assertEquals(10, tshirt.getStock(Size.M));
    }

    @Test
    void shouldAcceptShoesReturnAfterFourteenDays() {
        // POLYMORPHISME : 20 jours après la commande, un vêtement serait refusé
        // mais des chaussures sont acceptées (délai de 30 jours)
        Order order = shippedOrder(shoes, Size.M, 1);
        assertDoesNotThrow(() ->
                returnService.processReturn(order, order.getOrderDate().plusDays(20)));
    }

    // CAS LIMITE : le dernier jour autorisé
    @Test
    void shouldAcceptReturnOnLastAllowedDay() {
        Order order = shippedOrder(tshirt, Size.M, 1);
        returnService.processReturn(order, order.getOrderDate().plusDays(14));
        assertEquals(OrderStatus.RETURNED, order.getStatus());
    }

    // CAS INVALIDES
    @Test
    void shouldRefuseReturnOneDayAfterDeadline() {
        Order order = shippedOrder(tshirt, Size.M, 3);

        assertThrows(ReturnPeriodExpiredException.class, () ->
                returnService.processReturn(order, order.getOrderDate().plusDays(15)));
        // Retour refusé : le statut et le stock ne bougent pas
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals(7, tshirt.getStock(Size.M));
    }

    @Test
    void shouldRefuseReturnOfAccessory() {
        Order order = shippedOrder(cap, Size.ONE_SIZE, 1);
        assertThrows(ReturnNotAllowedException.class, () -> returnService.processReturn(order));
    }

    @Test
    void shouldRefuseWholeOrderIfOneItemCannotBeReturned() {
        // Modèle simplifié : un retour concerne toute la commande
        Cart cart = new Cart();
        cart.addItem(tshirt, Size.M, 2);
        cart.addItem(cap, Size.ONE_SIZE, 1);
        Order order = orderService.placeOrder(cart, customer);
        orderService.payOrder(order);
        orderService.shipOrder(order);

        assertThrows(ReturnNotAllowedException.class, () -> returnService.processReturn(order));
        assertEquals(8, tshirt.getStock(Size.M));
    }

    @Test
    void shouldRefuseReturnOfOrderThatIsNotShipped() {
        Cart cart = new Cart();
        cart.addItem(tshirt, Size.M, 1);
        Order order = orderService.placeOrder(cart, customer);

        assertThrows(IllegalStateException.class, () -> returnService.processReturn(order));
        assertEquals(9, tshirt.getStock(Size.M));
    }

    @Test
    void shouldRefuseReturningTheSameOrderTwice() {
        Order order = shippedOrder(tshirt, Size.M, 3);
        returnService.processReturn(order);

        assertThrows(IllegalStateException.class, () -> returnService.processReturn(order));
        // Le stock n'est restitué qu'une seule fois
        assertEquals(10, tshirt.getStock(Size.M));
    }
}
