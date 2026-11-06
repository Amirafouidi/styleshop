package com.example.styleshop.utils;

import com.example.styleshop.exception.InvalidProductDataException;
import com.example.styleshop.model.Clothing;
import com.example.styleshop.model.Product;
import com.example.styleshop.model.Shoes;
import com.example.styleshop.model.Size;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvProductLoaderTest {

    private static final String HEADER = "type,id,name,price,size,stock\n";

    private final CsvProductLoader loader = new CsvProductLoader();

    // Méthode d'aide : on charge un texte écrit dans le test, comme si c'était un fichier
    private List<Product> load(String csvContent) {
        return loader.load(new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
    }

    // CAS NORMAL : deux lignes du même produit sont regroupées, et le type crée la bonne sous-classe
    @Test
    void shouldGroupSizesOfSameProductAndCreateRightType() {
        List<Product> products = load(HEADER
                + "CLOTHING,C1,T-shirt blanc,19.90,S,10\n"
                + "CLOTHING,C1,T-shirt blanc,19.90,M,15\n"
                + "SHOES,S1,Baskets urbaines,59.90,M,5\n");

        assertEquals(2, products.size());
        assertInstanceOf(Clothing.class, products.get(0));
        assertEquals(10, products.get(0).getStock(Size.S));
        assertEquals(15, products.get(0).getStock(Size.M));
        assertInstanceOf(Shoes.class, products.get(1));
        assertTrue(loader.getErrors().isEmpty());
    }

    // CAS LIMITES : lignes vides ignorées, fichier avec seulement l'en-tête
    @Test
    void shouldIgnoreBlankLines() {
        List<Product> products = load(HEADER + "\nCLOTHING,C1,T-shirt,19.90,S,10\n\n");
        assertEquals(1, products.size());
        assertTrue(loader.getErrors().isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenFileHasOnlyHeader() {
        assertTrue(load(HEADER).isEmpty());
    }

    // CAS INVALIDES : la ligne fautive est signalée avec son numéro, les autres sont chargées
    @Test
    void shouldReportLineNumberOfInvalidLine() {
        List<Product> products = load(HEADER
                + "CLOTHING,C1,T-shirt,19.90,S,10\n"
                + "CLOTHING,C2,Jean,49.90,M\n");

        assertEquals(1, products.size());
        assertEquals(1, loader.getErrors().size());
        assertTrue(loader.getErrors().get(0).contains("Ligne 3"));
    }

    @Test
    void shouldIgnoreAllInvalidLinesAndKeepValidOnes() {
        List<Product> products = load(HEADER
                + "CLOTHING,C1,T-shirt,19.90,S,10\n"       // valide
                + "CLOTHING,C2,Jean,abc,M,5\n"             // prix non numérique
                + "CLOTHING,C3,Pull,-5,M,5\n"              // prix négatif
                + "CLOTHING,C4,Veste,80.0,XXL,5\n"         // taille inconnue
                + "BAG,B1,Sac,30.0,M,5\n"                  // type inconnu
                + "SHOES,S1,Baskets,59.90,M,5\n");         // valide

        assertEquals(2, products.size());
        assertEquals(4, loader.getErrors().size());
    }

    @Test
    void shouldThrowWhenFileIsMissing() {
        assertThrows(InvalidProductDataException.class,
                () -> loader.loadFromResource("inexistant.csv"));
    }

    // Test sur le vrai fichier du projet
    @Test
    void shouldLoadRealCatalogWithoutErrors() {
        List<Product> products = loader.loadFromResource("products.csv");
        assertTrue(products.size() > 0);
        assertTrue(loader.getErrors().isEmpty());
    }
}
