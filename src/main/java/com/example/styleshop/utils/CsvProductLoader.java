package com.example.styleshop.utils;

import com.example.styleshop.exception.InvalidProductDataException;
import com.example.styleshop.model.Accessory;
import com.example.styleshop.model.Clothing;
import com.example.styleshop.model.Product;
import com.example.styleshop.model.Shoes;
import com.example.styleshop.model.Size;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// SRP : cette classe n'a qu'un rôle, transformer le fichier CSV en objets Product.
// Elle ne connaît ni le panier, ni les commandes, ni l'affichage.
public class CsvProductLoader {

    // Lignes ignorées pendant le dernier chargement, pour pouvoir les signaler
    private final List<String> errors = new ArrayList<>();

    // CHARGEMENT AU DÉMARRAGE : le fichier est dans src/main/resources, donc il est
    // trouvé par le classloader, quel que soit l'endroit d'où on lance le programme
    public List<Product> loadFromResource(String resourceName) {
        InputStream stream = CsvProductLoader.class.getClassLoader().getResourceAsStream(resourceName);
        if (stream == null) {
            // Cas grave : sans fichier, pas de catalogue
            throw new InvalidProductDataException("Fichier introuvable : " + resourceName);
        }
        return load(stream);
    }

    // On reçoit un InputStream plutôt qu'un nom de fichier : cela permet de tester la lecture
    // avec un faux fichier en mémoire, sans dépendre du vrai products.csv
    public List<Product> load(InputStream inputStream) {
        errors.clear();
        // COLLECTION Map : une entrée par id. Les lignes d'un même produit (une par taille)
        // sont regroupées. LinkedHashMap garde l'ordre du fichier.
        Map<String, Product> products = new LinkedHashMap<>();

        // try-with-resources : le fichier est fermé automatiquement, même en cas d'erreur
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.readLine(); // on saute la ligne d'en-tête
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    parseLine(line, lineNumber, products);
                } catch (InvalidProductDataException e) {
                    // Une ligne fausse ne doit pas empêcher de charger les autres produits
                    errors.add(e.getMessage());
                }
            }
        } catch (IOException e) {
            // Exception standard transformée en exception non vérifiée avec un message clair
            throw new UncheckedIOException("Erreur de lecture du fichier", e);
        }
        return new ArrayList<>(products.values());
    }

    // Copie non modifiable : l'extérieur peut lire les erreurs sans toucher à la liste interne
    public List<String> getErrors() {
        return List.copyOf(errors);
    }

    private void parseLine(String line, int lineNumber, Map<String, Product> products) {
        String[] parts = line.split(",");
        if (parts.length != 6) {
            throw new InvalidProductDataException(
                    "Ligne " + lineNumber + " : 6 colonnes attendues, " + parts.length + " trouvées.");
        }
        try {
            String type = parts[0].trim().toUpperCase();
            String id = parts[1].trim();
            String name = parts[2].trim();
            double price = Double.parseDouble(parts[3].trim());
            Size size = Size.valueOf(parts[4].trim().toUpperCase());
            int stock = Integer.parseInt(parts[5].trim());

            // Premier passage sur cet id : on crée le produit. Sinon on ajoute seulement une taille.
            Product product = products.get(id);
            if (product == null) {
                product = createProduct(type, id, name, price);
                products.put(id, product);
            }
            product.addStock(size, stock);
        } catch (IllegalArgumentException e) {
            // NumberFormatException hérite de IllegalArgumentException : un seul catch suffit
            // pour un prix ou un stock non numérique, une taille ou un type inconnu.
            // On le transforme en NOTRE exception, avec le numéro de ligne.
            throw new InvalidProductDataException("Ligne " + lineNumber + " invalide : " + e.getMessage());
        }
    }

    // POLYMORPHISME : on renvoie un Product, le vrai type dépend de la colonne "type" du CSV.
    // Le reste du programme n'a pas besoin de savoir si c'est un vêtement ou des chaussures.
    private Product createProduct(String type, String id, String name, double price) {
        return switch (type) {
            case "CLOTHING" -> new Clothing(id, name, price);
            case "SHOES" -> new Shoes(id, name, price);
            case "ACCESSORY" -> new Accessory(id, name, price);
            default -> throw new IllegalArgumentException("type inconnu : " + type);
        };
    }
}