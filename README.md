# 🔍 Lost & Found Management System

A fully object-oriented **Java console application** for managing a Lost and Found department. Supports registering found items, intelligent multi-criteria matching when someone reports a loss, claim processing, and administrative reporting.

---

## 📁 Project Structure

```
LostAndFound/
├── pom.xml
└── src/
    ├── main/java/com/lostandfound/
    │   ├── Main.java                          ← Entry point
    │   ├── enums/
    │   │   ├── Category.java                  ← Item categories (Electronics, Bags, etc.)
    │   │   └── Status.java                    ← Item lifecycle status
    │   ├── model/
    │   │   ├── Item.java                      ← Abstract base class
    │   │   ├── FoundItem.java                 ← Item found and submitted
    │   │   ├── LostItem.java                  ← Item reported as lost
    │   │   ├── Location.java                  ← Location with similarity scoring
    │   │   ├── Person.java                    ← Person (finder / claimant / owner)
    │   │   ├── MatchResult.java               ← Match score + breakdown
    │   │   └── ClaimRecord.java               ← Claim transaction receipt
    │   ├── repository/
    │   │   ├── Repository.java                ← Generic CRUD interface
    │   │   ├── FoundItemRepository.java       ← Found item storage & queries
    │   │   ├── LostItemRepository.java        ← Lost report storage & queries
    │   │   └── ClaimRecordRepository.java     ← Claim history storage
    │   ├── service/
    │   │   ├── LostAndFoundService.java       ← Main business logic orchestrator
    │   │   ├── MatchingEngine.java            ← Weighted multi-criteria matching
    │   │   └── NotificationService.java       ← Console-based notifications
    │   ├── ui/
    │   │   └── ConsoleUI.java                 ← Interactive menu-driven interface
    │   └── util/
    │       ├── DataSeeder.java                ← Sample data loader
    │       └── InputHelper.java               ← Safe, validated console input
    └── test/java/com/lostandfound/
        └── MatchingEngineTest.java            ← 10 standalone unit tests
```

---

## 🏛️ OOP Design

### Class Hierarchy

```
Item  (abstract)
├── FoundItem      — item discovered and handed in
└── LostItem       — item reported missing by owner
```

### Key OOP Principles Applied

| Principle | How it's used |
|-----------|--------------|
| **Abstraction** | `Item` is abstract; `getSummary()` is abstract and implemented differently in each subclass |
| **Inheritance** | `FoundItem` and `LostItem` extend `Item`, sharing common fields |
| **Encapsulation** | All fields are `private`/`protected` with getters/setters |
| **Polymorphism** | `getSummary()` called uniformly on any `Item` reference, outputs type-specific text |
| **Interface** | `Repository<T, ID>` defines a generic contract; three concrete implementations |
| **Composition** | `FoundItem` contains `Location`, `Person`; `LostAndFoundService` owns all repositories |
| **Separation of Concerns** | UI, business logic, matching, persistence, and notifications in distinct layers |

---

## 🧠 Matching Algorithm

When someone reports a lost item, `MatchingEngine` scores it against every active found item using **six weighted criteria**:

| Criterion | Weight | Description |
|-----------|--------|-------------|
| Category  | 25%    | Exact category match required for high score |
| Name/Desc | 25%    | Jaccard keyword similarity between name and description |
| Color     | 15%    | Exact or partial color match |
| Brand     | 10%    | Exact or partial brand match |
| Location  | 15%    | Building → Floor → Area similarity |
| Time      | 10%    | Time window plausibility (item found after loss, within 72h window) |

**Confidence Levels:**

| Score Range | Level  | Stars |
|-------------|--------|-------|
| ≥ 70%       | HIGH   | ★★★   |
| 45–69%      | MEDIUM | ★★☆   |
| 25–44%      | LOW    | ★☆☆   |
| < 25%       | NO MATCH | ☆☆☆ |

---

## 🚀 How to Build & Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build
```bash
mvn clean package
```
This produces `target/lostandfound.jar`.

### Run (with sample data)
```bash
java -jar target/lostandfound.jar
```

### Run (without sample data)
```bash
java -jar target/lostandfound.jar --no-seed
```

### Run Tests
```bash
# Compile and run the manual test suite
mvn compile test-compile
java -cp target/classes:target/test-classes com.lostandfound.MatchingEngineTest
```

---

## 📋 Usage Guide

### Main Menu
```
[1] Staff Portal   — Register found items, process claims
[2] Public Portal  — Report a lost item / check for a match
[3] Admin Panel    — View records, statistics, search
[0] Exit
```

### Staff Portal
1. **Register a Found Item** — Enter item details, location, time found, and photo path. The system automatically checks existing lost reports for an immediate match.
2. **Process a Claim** — When an owner comes to collect, select the item ID, enter claimant details, and a receipt is generated.
3. **View All Found Items** — Table view of all registered found items.
4. **Update Item Status** — Change status (Found → Matched → Claimed → Expired).

### Public Portal (Person checking in)
1. Enter your contact details.
2. Describe your lost item (name, category, colour, brand).
3. Optionally attach a photo path.
4. Enter where and when you think you lost it.
5. The system searches the database and shows match results with confidence scores.
6. If a match is found, you are directed to the Lost & Found office to claim it.
7. If no match, your report is saved — you receive a reference ID for follow-up.

### Admin Panel
- System statistics (totals, matched, awaiting claim)
- View all lost reports
- View all claim records
- Keyword search across found and lost items
- View full item details by ID
- Manual re-match trigger for existing lost reports

---

## 🧪 Test Cases

The test suite (`MatchingEngineTest.java`) covers 10 scenarios:

| # | Test |
|---|------|
| 1 | Perfect match → HIGH confidence |
| 2 | Category mismatch → score < 0.70 |
| 3 | Time window: found soon > found late |
| 4 | Same location scores higher than different location |
| 5 | Completely different items → NO MATCH |
| 6 | Partial description overlap → MEDIUM match |
| 7 | Color + brand match boosts overall score |
| 8 | Multiple found items returned ranked (best first) |
| 9 | Service check-in with existing match in DB |
| 10 | Service check-in with no match → report saved |

---

## 📦 Item Categories

Electronics, Clothing & Accessories, Documents & Cards, Jewelry & Watches, Bags & Luggage, Keys, Books & Stationery, Sports Equipment, Toys & Games, Medical Devices, Money & Wallets, Other

---

## 📌 Item Lifecycle

```
[Found] ──▶ [Matched] ──▶ [Claimed]
   │
   └──▶ [Expired]  (after 90 days if unclaimed)
```

```
[Lost] ──▶ [Matched] ──▶ (owner visits to claim)
  │
  └──▶ stays [Lost] until a future found item matches
```

---

## 🔧 Extending the System

- **File persistence**: Implement `save()`/`load()` using Java serialization or JSON (Gson/Jackson) in each repository.
- **GUI**: Replace `ConsoleUI` with a JavaFX or Swing front-end; the service layer doesn't change.
- **Database**: Swap in-memory `HashMap` stores for JDBC/JPA implementations of the `Repository<T,ID>` interface.
- **Image matching**: Integrate a vision API call in `MatchingEngine` to compare uploaded item photos.
- **Email/SMS notifications**: Replace `NotificationService` print statements with real email (JavaMail) or SMS API calls.
