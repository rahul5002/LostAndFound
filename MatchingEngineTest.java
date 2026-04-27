package com.lostandfound;

import com.lostandfound.enums.Category;
import com.lostandfound.model.*;
import com.lostandfound.service.MatchingEngine;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Manual test suite for the MatchingEngine and core workflows.
 * Run directly: java -cp ... com.lostandfound.MatchingEngineTest
 *
 * (No JUnit dependency needed — uses plain assertions so the project
 *  compiles with zero external dependencies.)
 */
public class MatchingEngineTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║      MATCHING ENGINE — TEST SUITE            ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        testPerfectMatch();
        testCategoryMismatchLowScore();
        testTimeWindowScoring();
        testLocationSimilarity();
        testNoMatchBelowThreshold();
        testPartialDescriptionMatch();
        testColorBrandMatch();
        testMultipleFoundItemsRanked();
        testServiceCheckInWithMatch();
        testServiceCheckInNoMatch();

        System.out.println("\n══════════════════════════════════════════════");
        System.out.printf("  Results: %d passed, %d failed out of %d tests%n",
                passed, failed, passed + failed);
        System.out.println("══════════════════════════════════════════════");
        if (failed > 0) System.exit(1);
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    static void testPerfectMatch() {
        String test = "Perfect match (same category, name, color, brand, location, time)";
        try {
            MatchingEngine engine = new MatchingEngine();
            Person p = person("Alice");
            Location loc = new Location("Library", "First", "Reading Hall", null);

            LocalDateTime lostTime = LocalDateTime.now().minusDays(1);

            LostItem lost = lostItem("iPhone 13", "Black iPhone with blue case",
                    Category.ELECTRONICS, "Black", "Apple", loc, p, lostTime, null);

            FoundItem found = foundItem("iPhone 13", "Black iPhone blue case cracked screen",
                    Category.ELECTRONICS, "Black", "Apple", loc, lostTime.plusHours(3));

            MatchResult result = engine.computeMatch(lost, found);

            assertTrue(test, result.getOverallScore() >= 0.70,
                    "Expected HIGH match (>=0.70), got: " + result.getOverallScore());
            assertTrue(test + " [confidence]", result.isStrongMatch(),
                    "Expected strong match flag to be true");
            pass(test);
        } catch (AssertionError e) {
            fail(test, e.getMessage());
        }
    }

    static void testCategoryMismatchLowScore() {
        String test = "Category mismatch → low score";
        try {
            MatchingEngine engine = new MatchingEngine();
            Person p = person("Bob");
            Location loc = new Location("Gym", "Ground", "Locker", null);
            LocalDateTime t = LocalDateTime.now().minusHours(5);

            LostItem lost = lostItem("Wallet", "Brown leather wallet",
                    Category.MONEY, "Brown", null, loc, p, t, null);

            FoundItem found = foundItem("Wallet", "Brown leather wallet",
                    Category.ELECTRONICS, "Brown", null, loc, t.plusHours(1));

            MatchResult result = engine.computeMatch(lost, found);
            assertTrue(test, result.getOverallScore() < 0.70,
                    "Category mismatch should reduce score below 0.70, got: " + result.getOverallScore());
            pass(test);
        } catch (AssertionError e) {
            fail(test, e.getMessage());
        }
    }

    static void testTimeWindowScoring() {
        String test = "Found within 24h → full time score; found after 30 days → low time score";
        try {
            MatchingEngine engine = new MatchingEngine();
            Person p = person("Carol");
            Location loc = new Location("Cafeteria", "Ground", "Hall", null);
            LocalDateTime lostTime = LocalDateTime.now().minusDays(35);

            LostItem lost = lostItem("Keys", "Silver keys with red keychain",
                    Category.KEYS, "Silver", null, loc, p, lostTime, null);

            // Found very soon after loss
            FoundItem foundSoon = foundItem("Keys", "Silver keys red keychain",
                    Category.KEYS, "Silver", null, loc, lostTime.plusHours(12));

            // Found 35 days later
            FoundItem foundLate = foundItem("Keys", "Silver keys red keychain",
                    Category.KEYS, "Silver", null, loc, LocalDateTime.now());

            MatchResult resSoon = engine.computeMatch(lost, foundSoon);
            MatchResult resLate = engine.computeMatch(lost, foundLate);

            assertTrue(test + " [soon]", resSoon.getOverallScore() > resLate.getOverallScore(),
                    "Soon-found item should score higher. Soon: " + resSoon.getOverallScore()
                            + " Late: " + resLate.getOverallScore());
            pass(test);
        } catch (AssertionError e) {
            fail(test, e.getMessage());
        }
    }

    static void testLocationSimilarity() {
        String test = "Same building+area → higher score than different building";
        try {
            MatchingEngine engine = new MatchingEngine();
            Person p = person("Dave");
            LocalDateTime t = LocalDateTime.now().minusHours(10);

            Location lostLoc  = new Location("Main Building", "Ground", "Cafeteria", null);
            Location sameLoc  = new Location("Main Building", "Ground", "Cafeteria", null);
            Location diffLoc  = new Location("Sports Block",  "First",  "Gymnasium", null);

            LostItem lost = lostItem("Backpack", "Red backpack",
                    Category.BAGS, "Red", null, lostLoc, p, t, null);

            FoundItem foundSame = foundItem("Backpack", "Red backpack",
                    Category.BAGS, "Red", null, sameLoc, t.plusHours(1));
            FoundItem foundDiff = foundItem("Backpack", "Red backpack",
                    Category.BAGS, "Red", null, diffLoc, t.plusHours(1));

            MatchResult resSame = engine.computeMatch(lost, foundSame);
            MatchResult resDiff = engine.computeMatch(lost, foundDiff);

            assertTrue(test, resSame.getOverallScore() > resDiff.getOverallScore(),
                    "Same location should score higher. Same: " + resSame.getOverallScore()
                            + " Diff: " + resDiff.getOverallScore());
            pass(test);
        } catch (AssertionError e) {
            fail(test, e.getMessage());
        }
    }

    static void testNoMatchBelowThreshold() {
        String test = "Completely different items → no match";
        try {
            MatchingEngine engine = new MatchingEngine();
            Person p = person("Eve");
            LocalDateTime t = LocalDateTime.now().minusDays(10);

            LostItem lost = lostItem("Laptop", "Silver MacBook Pro 14 inch",
                    Category.ELECTRONICS, "Silver", "Apple",
                    new Location("Library", "Second", "Study Room", null), p, t, null);

            FoundItem found = foundItem("Football", "White and black football",
                    Category.SPORTS, "White", "Nike",
                    new Location("Sports Complex", "Ground", "Field", null),
                    t.plusDays(5));

            MatchResult result = engine.computeMatch(lost, found);
            assertTrue(test, !result.isMatch(),
                    "Expected no match, got score: " + result.getOverallScore());
            pass(test);
        } catch (AssertionError e) {
            fail(test, e.getMessage());
        }
    }

    static void testPartialDescriptionMatch() {
        String test = "Partial description overlap → medium match";
        try {
            MatchingEngine engine = new MatchingEngine();
            Person p = person("Frank");
            LocalDateTime t = LocalDateTime.now().minusHours(8);
            Location loc = new Location("Parking", "Ground", "Block B", null);

            LostItem lost = lostItem("Car Key", "Honda car key with remote and keychain",
                    Category.KEYS, "Silver", "Honda", loc, p, t, null);

            FoundItem found = foundItem("Keys", "Car key Honda remote silver",
                    Category.KEYS, "Silver", "Honda", loc, t.plusHours(2));

            MatchResult result = engine.computeMatch(lost, found);
            assertTrue(test, result.getOverallScore() >= 0.45,
                    "Expected medium+ match (>=0.45), got: " + result.getOverallScore());
            pass(test);
        } catch (AssertionError e) {
            fail(test, e.getMessage());
        }
    }

    static void testColorBrandMatch() {
        String test = "Color + brand match boosts score";
        try {
            MatchingEngine engine = new MatchingEngine();
            Person p = person("Grace");
            LocalDateTime t = LocalDateTime.now().minusHours(2);
            Location loc = new Location("Gym", "Ground", "Locker", null);

            LostItem lostWithBrand = lostItem("Watch", "Digital sports watch",
                    Category.JEWELRY, "Black", "Casio", loc, p, t, null);

            LostItem lostNoBrand = lostItem("Watch", "Digital sports watch",
                    Category.JEWELRY, "Black", null, loc, p, t, null);

            FoundItem found = foundItem("Watch", "Casio digital sports watch black strap",
                    Category.JEWELRY, "Black", "Casio", loc, t.plusHours(1));

            MatchResult withBrand = engine.computeMatch(lostWithBrand, found);
            MatchResult noBrand   = engine.computeMatch(lostNoBrand, found);

            assertTrue(test, withBrand.getOverallScore() >= noBrand.getOverallScore(),
                    "Brand match should not decrease score. With: " + withBrand.getOverallScore()
                            + " Without: " + noBrand.getOverallScore());
            pass(test);
        } catch (AssertionError e) {
            fail(test, e.getMessage());
        }
    }

    static void testMultipleFoundItemsRanked() {
        String test = "Multiple found items returned in descending score order";
        try {
            MatchingEngine engine = new MatchingEngine();
            Person p = person("Henry");
            LocalDateTime t = LocalDateTime.now().minusDays(1);
            Location loc = new Location("Main Building", "Ground", "Cafeteria", null);

            LostItem lost = lostItem("iPhone 13", "Black iPhone 13 Pro in blue case",
                    Category.ELECTRONICS, "Black", "Apple", loc, p, t, null);

            FoundItem f1 = foundItem("iPhone 13", "Black iPhone blue case",
                    Category.ELECTRONICS, "Black", "Apple", loc, t.plusHours(2));  // best
            FoundItem f2 = foundItem("Smartphone", "Black phone",
                    Category.ELECTRONICS, "Black", null,
                    new Location("Library", "First", "Hall", null), t.plusHours(6)); // medium
            FoundItem f3 = foundItem("Tablet", "White iPad",
                    Category.ELECTRONICS, "White", "Apple",
                    new Location("Parking", "Ground", "Lot", null), t.plusDays(3));  // lowest

            List<MatchResult> results = engine.match(lost, Arrays.asList(f1, f2, f3));

            assertTrue(test + " [non-empty]", !results.isEmpty(), "Expected at least 1 match");
            for (int i = 0; i < results.size() - 1; i++) {
                assertTrue(test + " [order " + i + "]",
                        results.get(i).getOverallScore() >= results.get(i + 1).getOverallScore(),
                        "Results not sorted descending");
            }
            pass(test);
        } catch (AssertionError e) {
            fail(test, e.getMessage());
        }
    }

    static void testServiceCheckInWithMatch() {
        String test = "Service.checkInForLostItem → returns matches when item exists in DB";
        try {
            com.lostandfound.service.LostAndFoundService service =
                    new com.lostandfound.service.LostAndFoundService();

            Person finder = person("Finder");
            Location loc = new Location("Cafeteria", "Ground", "Hall", null);
            LocalDateTime foundTime = LocalDateTime.now().minusDays(1);

            FoundItem found = new FoundItem("Laptop", "Silver Dell laptop 15 inch",
                    Category.ELECTRONICS, "Silver", "Dell", null, loc, finder,
                    foundTime, "Found on table", "Office");
            service.registerFoundItem(found);

            Person owner = person("Owner");
            LostItem lost = lostItem("Laptop", "Silver Dell 15 inch laptop",
                    Category.ELECTRONICS, "Silver", "Dell", loc, owner,
                    foundTime.minusHours(5), null);

            List<MatchResult> matches = service.checkInForLostItem(lost);

            assertTrue(test, !matches.isEmpty(), "Expected at least 1 match from service");
            pass(test);
        } catch (AssertionError e) {
            fail(test, e.getMessage());
        }
    }

    static void testServiceCheckInNoMatch() {
        String test = "Service.checkInForLostItem → registers lost report when no match";
        try {
            com.lostandfound.service.LostAndFoundService service =
                    new com.lostandfound.service.LostAndFoundService();

            Person owner = person("Owner2");
            Location loc = new Location("Library", "Second", "Study Area", null);
            LocalDateTime t = LocalDateTime.now().minusHours(3);

            LostItem lost = lostItem("Rare Antique Watch", "Very unique 100-year-old watch",
                    Category.JEWELRY, "Gold", "Rolex", loc, owner, t, null);

            List<MatchResult> matches = service.checkInForLostItem(lost);

            assertTrue(test, matches.isEmpty(), "Expected no matches for unique item not in DB");

            // Verify it was saved as a lost report
            var savedLost = service.getLostItemById(lost.getItemId());
            assertTrue(test + " [saved]", savedLost.isPresent(),
                    "Lost item should be saved to DB after no match");
            pass(test);
        } catch (AssertionError e) {
            fail(test, e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    static Person person(String name) {
        return new Person("T-" + name, name, "0000000000", null, null);
    }

    static LostItem lostItem(String name, String desc, Category cat,
                              String color, String brand, Location loc,
                              Person owner, LocalDateTime start, LocalDateTime end) {
        return new LostItem(name, desc, cat, color, brand, null, loc, owner, start, end, null, null);
    }

    static FoundItem foundItem(String name, String desc, Category cat,
                                String color, String brand, Location loc, LocalDateTime foundTime) {
        return new FoundItem(name, desc, cat, color, brand, null, loc,
                person("Staff"), foundTime, null, "Office");
    }

    static void assertTrue(String test, boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    static void pass(String test) {
        System.out.println("  ✅  PASS : " + test);
        passed++;
    }

    static void fail(String test, String reason) {
        System.out.println("  ❌  FAIL : " + test);
        System.out.println("           → " + reason);
        failed++;
    }
}
