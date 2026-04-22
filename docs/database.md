# Database Design (MongoDB)

## Überblick

Dieses Dokument beschreibt die Datenbankstruktur des Systems basierend auf der Architektur und den Domain-Modellen.

Die Datenbank basiert auf **MongoDB** und nutzt eine Kombination aus:

* Referenzen (`id`)
* eingebetteten Dokumenten (z. B. `OrderItem`)

---

## Collections

### 1. users

```json
{
  "id": "user-123",
  "role": "CUSTOMER",
  "name": "Max Mustermann",
  "email": "max@example.com",
  "oauthProvider": "GITHUB",
  "oauthProviderUserId": "github-123",
  "createdAt": "...",
  "updatedAt": "...",

  // seller only
  "businessName": "...",
  "taxId": "...",
  "onboardingCompleted": true,

  // relations
  "shopId": "shop-123"
}
```

### Indizes

* unique: `email`
* unique: `oauthProviderUserId`

---

### 2. shops

```json
{
  "id": "shop-123",
  "sellerId": "user-123",
  "name": "Honigstube",
  "status": "ACTIVE",
  "invoiceIds": ["settlement-1"],
  "createdAt": "...",
  "updatedAt": "..."
}
```

### Indizes

* unique: `sellerId`

---

### 3. products

```json
{
  "id": "prod-123",
  "shopId": "shop-123",
  "sellerId": "user-123",
  "name": "Waldhonig",
  "price": 8.99,
  "category": "HONIG",
  "stockQuantity": 42,
  "status": "ACTIVE",
  "images": [...],
  "createdAt": "...",
  "updatedAt": "..."
}
```

### Indizes

* `shopId`
* `status`
* `category`
* Textindex auf `name`, `description`

---

### 4. carts

```json
{
  "id": "cart-123",
  "customerId": "user-123",
  "items": [
    {
      "productId": "prod-123",
      "quantity": 2
    }
  ]
}
```

---

### 5. fulfillment_orders

```json
{
  "id": "fo-123",
  "customerId": "user-123",
  "items": [OrderItem],
  "sellerOrderIds": ["so-1"],
  "totalPrice": 29.99,
  "isPaid": true,
  "status": "PROCESSING"
}
```

---

### 6. seller_orders

```json
{
  "id": "so-1",
  "fulfillmentOrderId": "fo-123",
  "sellerId": "user-456",
  "shopId": "shop-456",
  "items": [OrderItem],
  "status": "IN_PREPARATION"
}
```

---

### 7. customer_invoices

```json
{
  "id": "inv-123",
  "fulfillmentOrderId": "fo-123",
  "customerId": "user-123",
  "totalAmount": 29.99
}
```

---

### 8. seller_settlements

```json
{
  "id": "settlement-123",
  "shopId": "shop-123",
  "sellerId": "user-123",
  "periodStart": "2026-04-01",
  "periodEnd": "2026-04-30",
  "totalRevenue": 1000,
  "commissionAmount": 100
}
```

---

## Embedding Strategy

| Entity                | Strategy   |
| --------------------- | ---------- |
| OrderItem             | embedded   |
| Cart Items            | embedded   |
| Relations (User→Shop) | referenced |
| Orders → SellerOrders | referenced |

---

## Wichtige Regeln

* 1 Seller → 1 Shop
* 1 Customer → 1 Cart
* Orders speichern Snapshots
* Produkte werden nicht direkt referenziert (nur ID + Snapshot)
