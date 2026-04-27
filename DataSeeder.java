package com.lostandfound.util;

import com.lostandfound.enums.Category;
import com.lostandfound.model.*;
import com.lostandfound.service.LostAndFoundService;

import java.time.LocalDateTime;

/**
 * Populates the system with realistic sample data for demonstration and testing.
 */
public class DataSeeder {

    public static void seed(LostAndFoundService service) {
        System.out.println("\n  [DataSeeder] Loading sample data...");

        // ── People ───────────────────────────────────────────────────────────
        Person alice   = new Person("P001", "Alice Sharma",   "9876543210", "alice@email.com",   "12, MG Road, Dehradun");
        Person bob     = new Person("P002", "Bob Mehta",      "9123456780", "bob@email.com",     "34, Rajpur Road, Dehradun");
        Person carol   = new Person("P003", "Carol Singh",    "9988776655", "carol@email.com",   "56, Clock Tower, Dehradun");
        Person dave    = new Person("P004", "Dave Patel",     "9871234560", "dave@email.com",    "78, Saharanpur Road");
        Person finder1 = new Person("F001", "Security Guard", "1800000000", "security@fac.com",  "Main Gate");
        Person finder2 = new Person("F002", "Canteen Staff",  "1800000001", "canteen@fac.com",   "Canteen Block");
        Person finder3 = new Person("F003", "Library Staff",  "1800000002", "library@fac.com",   "Library");

        // ── Found Items ──────────────────────────────────────────────────────

        // 1. Black iPhone found near cafeteria
        FoundItem f1 = new FoundItem(
            "iPhone 13",
            "Black iPhone with cracked screen protector, blue case",
            Category.ELECTRONICS,
            "Black", "Apple",
            null,
            new Location("Main Building", "Ground", "Cafeteria", null),
            finder2,
            LocalDateTime.now().minusDays(2),
            "Found under a cafeteria table",
            "Lost & Found Office, Room 101"
        );

        // 2. Red backpack found at library
        FoundItem f2 = new FoundItem(
            "Backpack",
            "Red and black backpack, contains some books and a water bottle",
            Category.BAGS,
            "Red", "Wildcraft",
            null,
            new Location("Library Block", "First", "Reading Hall", null),
            finder3,
            LocalDateTime.now().minusDays(5),
            "Left on a reading table, no one claimed it after 2 hours",
            "Lost & Found Office, Room 101"
        );

        // 3. Car keys found in parking lot
        FoundItem f3 = new FoundItem(
            "Car Keys",
            "Silver car key with Honda logo and a small orange keychain",
            Category.KEYS,
            "Silver", "Honda",
            null,
            new Location("Parking Lot", "Ground", "Block A", null),
            finder1,
            LocalDateTime.now().minusDays(1),
            "Found on the ground near parking slot A-12",
            "Security Cabin"
        );

        // 4. Wristwatch found at sports complex
        FoundItem f4 = new FoundItem(
            "Wristwatch",
            "Black digital sports watch with silicone strap",
            Category.JEWELRY,
            "Black", "Casio",
            null,
            new Location("Sports Complex", "Ground", "Basketball Court", null),
            finder1,
            LocalDateTime.now().minusDays(3),
            "Found near the basketball court sideline",
            "Lost & Found Office, Room 101"
        );

        // 5. Aadhar card found at reception
        FoundItem f5 = new FoundItem(
            "Aadhar Card",
            "Government ID card belonging to someone, laminated",
            Category.DOCUMENTS,
            "White", null,
            null,
            new Location("Main Building", "Ground", "Reception", null),
            finder1,
            LocalDateTime.now().minusHours(6),
            "Someone left it at the reception desk",
            "Reception Desk"
        );

        // 6. Blue water bottle
        FoundItem f6 = new FoundItem(
            "Water Bottle",
            "Blue stainless steel insulated bottle, 1 litre",
            Category.OTHER,
            "Blue", "Milton",
            null,
            new Location("Gym", "Ground", "Locker Room", null),
            finder1,
            LocalDateTime.now().minusDays(4),
            "Left in the locker room",
            "Gym Reception"
        );

        // Register all found items
        service.registerFoundItem(f1);
        service.registerFoundItem(f2);
        service.registerFoundItem(f3);
        service.registerFoundItem(f4);
        service.registerFoundItem(f5);
        service.registerFoundItem(f6);

        System.out.println("  [DataSeeder] " + 6 + " found items registered.");
        System.out.println("  [DataSeeder] Sample data loaded successfully!\n");
    }
}
