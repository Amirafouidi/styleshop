package com.example.styleshop.service;

import com.example.styleshop.exception.EmptyCartException;
import com.example.styleshop.exception.OutOfStockException;
import com.example.styleshop.model.Cart;
import com.example.styleshop.model.Clothing;
import com.example.styleshop.model.Customer;
import com.example.styleshop.model.Order;
import com.example.styleshop.model.OrderStatus;
import com.example.styleshop.model.Product;
import com.example.styleshop.model.Size;
import com.example.styleshop.model.StandardCustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceTest {

    private final OrderService orderService =
            new OrderService(new PricingService(new ShippingCalculator()));
    private final Customer customer = new StandardCustomer("U1", "Alice", "alice@mail.com");
    private final Product tshirt = new Clothing("C1", "T-shirt", 20.0);
    private final Cart cart = new Cart();

    // JUnit crée une nouvelle instance de la classe pour chaque test :
    // chaque test repart donc avec un T-shirt à 10 en taille M et un panier vide
    @BeforeEach
    void setUp() {
        tshirt.addStock(Size.M, 10);
    }

    // CAS NORMAUX
    @Test
    void shouldReduceStockWhenOrderIsPlaced() {
        cart.addItem(tshirt, Size.M, 3);
        orderService.placeOrder(cart, customer);
        assertEquals(7, tshirt.getStock(Size.M));
    }

    @Test
    void shouldCreatePendingOrderWithTotalAndEmptyCart() {
        cart.addItem(tshirt, Size.M, 2);
        Order order = orderService.placeOrder(cart, customer);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        // 2 x 20 = 40 (sous le seuil de 50 pour un client standard) + 4,90 de livraison
        assertEquals(44.90, order.getTotal(), 0.001);
        assertTrue(cart.isEmpty());
    }

    @Test
    void shouldRestoreStockWhenOrderIsCancelled() {
        cart.addItem(tshirt, Size.M, 3);
        Order order = orderService.placeOrder(cart, customer);
        orderService.cancelOrder(order);
        assertEquals(10, tshirt.getStock(Size.M));
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    // CAS LIMITE : commander tout le stock restant
    @Test
    void shouldAllowOrderingAllRemainingStock() {
        cart.addItem(tshirt, Size.M, 10);
        orderService.placeOrder(cart, customer);
        assertEquals(0, tshirt.getStock(Size.M));
    }

    // CAS INVALIDES
    @Test
    void shouldThrowWhenCartIsEmpty() {
        assertThrows(EmptyCartException.class, () -> orderService.placeOrder(cart, customer));
    }

    @Test
    void shouldThrowAndKeepStockWhenStockIsNoLongerSufficient() {
        cart.addItem(tshirt, Size.M, 8);
        // Un autre client achète entre-temps : il ne reste plus que 5 articles
        tshirt.reduceStock(Size.M, 5);

        assertThrows(OutOfStockException.class, () -> orderService.placeOrder(cart, customer));
        assertEquals(5, tshirt.getStock(Size.M));
        assertFalse(cart.isEmpty());
    }

    @Test
    void shouldNotRemoveAnyStockIfOneItemIsOutOfStock() {
        Product jeans = new Clothing("C2", "Jean", 50.0);
        jeans.addStock(Size.L, 5);
        cart.addItem(tshirt, Size.M, 2);
        cart.addItem(jeans, Size.L, 5);
        // Le jean n'est plus disponible en quantité suffisante
        jeans.reduceStock(Size.L, 3);

        assertThrows(OutOfStockException.class, () -> orderService.placeOrder(cart, customer));
        // Tout ou rien : le stock du T-shirt n'a pas bougé
        assertEquals(10, tshirt.getStock(Size.M));
    }

    @Test
    void shouldNotCancelShippedOrder() {
        cart.addItem(tshirt, Size.M, 3);
        Order order = orderService.placeOrder(cart, customer);
        orderService.payOrder(order);
        orderService.shipOrder(order);

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(order));
        // Le stock n'est pas restitué si l'annulation est refusée
        assertEquals(7, tshirt.getStock(Size.M));
    }
}
