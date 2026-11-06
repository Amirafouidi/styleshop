# StyleShop

Boutique de vêtements en ligne en console, réalisée en Java pour le projet final de programmation orientée objet.

Auteur : FOUIDI Amira

## Objectif de l'application

StyleShop simule une boutique de vêtements. Le catalogue est chargé depuis un fichier CSV au démarrage. Un client ajoute des articles à son panier (par taille), bénéficie de remises, valide sa commande si le stock le permet, puis peut la payer, l'annuler ou la retourner sous conditions.

## Lancer l'application

Prérequis : JDK 21 et Maven.

- Depuis IntelliJ : ouvrir le projet et lancer la méthode `main` de `Application.java`.
- Depuis un terminal :

```
mvn compile
java -cp target/classes com.example.styleshop.Application
```

## Fonctionnalités principales

- Choix d'un client (Standard ou Premium)
- Consultation du catalogue avec le stock par taille
- Panier : ajout d'articles par taille, avec vérification du stock
- Validation du panier : création d'une commande (refusée si le panier est vide ou si le stock est insuffisant)
- Remises : fidélité (10 % pour un client Premium) et codes promo (`SOLDES20` : -20 %, `MOINS5` : -5 €)
- Livraison à 4,90 €, offerte à partir de 50 € (client Standard) ou 20 € (client Premium)
- Paiement, suivi et annulation d'une commande 
- Retour d'une commande : 14 jours pour un vêtement, 30 jours pour des chaussures, impossible pour un accessoire (le stock est remis à jour)

## Classes principales

| Package | Classes | Rôle |
|---|---|---|
| `model` | `Product`, `Clothing`, `Shoes`, `Accessory` | Produits, avec un stock par taille |
| `model` | `Customer`, `StandardCustomer`, `PremiumCustomer` | Clients et leurs avantages |
| `model` | `Cart`, `CartItem`, `Order`, `Size`, `OrderStatus` | Panier, commande, tailles, statuts |
| `discount` | `Discount`, `PercentageDiscount`, `FixedAmountDiscount` | Remises |
| `service` | `PricingService`, `OrderService`, `ReturnService`, `ShippingCalculator` | Logique métier |
| `utils` | `CsvProductLoader` | Lecture du fichier CSV |
| `exception` | 5 exceptions personnalisées | Erreurs métier |
| `ui` | `ConsoleMenu` | Interaction avec l'utilisateur |
| racine | `Application` | Charge les données, assemble les objets, lance le menu |

## Décisions techniques

1. **La date est un paramètre de `ReturnService.processReturn(order, today)`.** Une seconde version sans paramètre utilise la date du jour. Cela permet de tester le dernier jour autorisé et le lendemain, sans dépendre de l'horloge.
2. **Une commande est « tout ou rien ».** `OrderService.placeOrder` vérifie le stock de tous les articles avant d'en retirer un seul. Sinon, une erreur sur le dernier article laisserait les précédents débités. `ReturnService` fonctionne de la même façon : tous les articles sont vérifiés avant de toucher au stock.
3. **Une ligne CSV invalide ne bloque pas le chargement.** Elle est signalée (avec son numéro de ligne) puis ignorée. Seul un fichier introuvable arrête le programme, car sans catalogue l'application ne peut pas fonctionner.
4. **`Order` protège elle-même ses statuts.** Chaque transition (payer, expédier, annuler, retourner) vérifie le statut actuel et lève une `IllegalStateException` sinon. La règle est donc au même endroit pour tout le programme.
5. **Le total est arrondi au centime avant le calcul de la livraison.** Avec des `double`, un total de 50 € peut devenir 49,999999 et rater le seuil de livraison offerte.

Simplifications assumées : la commande est expédiée automatiquement au paiement, et un retour concerne toute la commande (si elle contient un accessoire, le retour est refusé).

## Où se trouvent les notions du cours

**Héritage**
- `Clothing`, `Shoes` et `Accessory` héritent de `Product`
- `StandardCustomer` et `PremiumCustomer` héritent de `Customer`

**Polymorphisme**
- `ReturnService` appelle `getReturnPeriodDays()` sur un `Product` : le résultat est 14, 30 ou 0 selon le type réel
- `PricingService` et `ShippingCalculator` appellent `getLoyaltyDiscountRate()` et `getFreeShippingThreshold()` sur un `Customer`
- `PricingService` appelle `apply()` sur un `Discount` sans savoir quelle remise c'est

**Classes abstraites et interface**
- `Product` et `Customer` sont abstraites : elles regroupent le code commun, et chaque sous-classe doit définir ses règles propres
- `Discount` est une interface : ses implémentations n'ont en commun que la méthode `apply`

## Exceptions personnalisées

| Exception | Levée quand |
|---|---|
| `OutOfStockException` | Le stock est insuffisant pour la taille demandée |
| `EmptyCartException` | On valide un panier vide |
| `ReturnPeriodExpiredException` | Le retour est demandé hors délai |
| `ReturnNotAllowedException` | L'article n'est pas retournable |
| `InvalidProductDataException` | Une ligne du CSV est incorrecte, ou le fichier est introuvable |

Ces exceptions sont attrapées dans `ConsoleMenu.run()` : l'utilisateur voit un message clair et le programme continue. Le code utilise aussi des exceptions standard (`IllegalArgumentException`, `IllegalStateException`) pour les valeurs invalides et les transitions interdites.

## Chargement des données

Au démarrage, `Application` appelle `CsvProductLoader` qui lit `src/main/resources/products.csv` ligne par ligne :

```
type,id,name,price,size,stock
CLOTHING,C1,T-shirt blanc,19.90,S,10
CLOTHING,C1,T-shirt blanc,19.90,M,15
SHOES,S1,Baskets urbaines,59.90,M,5
ACCESSORY,A1,Casquette,14.90,ONE_SIZE,20
```

La colonne `type` détermine la classe créée (`Clothing`, `Shoes` ou `Accessory`). Les lignes ayant le même `id` sont regroupées dans un seul produit, avec un stock par taille. Les lignes invalides sont signalées par un avertissement et ignorées.

## Application des principes SOLID

- **S (responsabilité unique)** : chaque classe a un seul rôle. `PricingService` calcule les prix, `OrderService` gère les commandes, `ReturnService` les retours, `ShippingCalculator` la livraison, `CsvProductLoader` la lecture du fichier, `ConsoleMenu` l'affichage. `Application` ne fait que démarrer le programme.
- **O (ouvert/fermé)** : ajouter un type de remise ou de produit se fait en créant une nouvelle classe, sans modifier le code existant.
- **L (substitution de Liskov)** : toute sous-classe de `Product` ou de `Customer` peut remplacer son parent sans casser le code.
- **I (ségrégation des interfaces)** : `Discount` est une interface minimale, avec une seule méthode.
- **D (inversion des dépendances), en partie** : les services reçoivent leurs dépendances par leur constructeur (assemblées dans `Application`) et `PricingService` travaille avec l'interface `Discount`. En revanche, certains services dépendent encore de classes concrètes, comme `ShippingCalculator`.

## Lancer les tests

Les tests utilisent JUnit 5 et portent sur la logique métier : remises, calcul du total, seuil de livraison, stock, annulation, retours, chargement du CSV. Ils couvrent des cas normaux, des cas limites (remise de 100 %, livraison pile au seuil, retour le dernier jour autorisé, stock à zéro) et des cas invalides (stock insuffisant, panier vide, retour hors délai, ligne CSV incorrecte).

- Depuis IntelliJ : clic droit sur le dossier `src/test/java` → **Run 'All Tests'**
- Depuis un terminal : `mvn test`

## Structure du projet

```
src/main/java/com/example/styleshop/
├── model/
├── discount/
├── service/
├── exception/
├── ui/
├── utils/
└── Application.java
src/main/resources/products.csv
src/test/java/com/example/styleshop/
README.md
```