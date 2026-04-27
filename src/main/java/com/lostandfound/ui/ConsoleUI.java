package com.lostandfound.ui;

import com.lostandfound.enums.Category;
import com.lostandfound.enums.Status;
import com.lostandfound.model.*;
import com.lostandfound.service.LostAndFoundService;
import com.lostandfound.service.LostAndFoundService.SystemStats;
import com.lostandfound.util.InputHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Full console-based UI for the Lost & Found Management System.
 * Provides three role-based menus: Staff, Public (person checking in), and Admin.
 */
public class ConsoleUI {

    private static final String LOGO =
        "\n" +
        "  ██╗      ██████╗ ███████╗████████╗     ██╗\n" +
        "  ██║     ██╔═══██╗██╔════╝╚══██╔══╝    ██╔╝\n" +
        "  ██║     ██║   ██║███████╗   ██║       ██╔╝ \n" +
        "  ██║     ██║   ██║╚════██║   ██║      ██╔╝  \n" +
        "  ███████╗╚██████╔╝███████║   ██║     ██╔╝   \n" +
        "  ╚══════╝ ╚═════╝ ╚══════╝   ╚═╝    ╚═╝    \n" +
        "         &  FOUND  MANAGEMENT  SYSTEM        \n";

    private static final String DIVIDER  = "═".repeat(54);
    private static final String DIVIDER2 = "─".repeat(54);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final LostAndFoundService service;
    private final Scanner scanner;
    private final InputHelper input;

    public ConsoleUI(LostAndFoundService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
        this.input   = new InputHelper(scanner);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Entry Point
    // ════════════════════════════════════════════════════════════════════════

    public void start() {
        clearScreen();
        System.out.println(LOGO);
        System.out.println("  " + DIVIDER);
        System.out.println("  Welcome to the Lost & Found Department System");
        System.out.println("  " + DIVIDER);

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = input.readInt("  Enter choice: ", 0, 3);
            switch (choice) {
                case 1 -> staffMenu();
                case 2 -> publicCheckInFlow();
                case 3 -> adminMenu();
                case 0 -> running = false;
            }
        }

        System.out.println("\n  Thank you for using the Lost & Found System. Goodbye!\n");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Main Menu
    // ════════════════════════════════════════════════════════════════════════

    private void printMainMenu() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("                    MAIN MENU");
        System.out.println("  " + DIVIDER);
        System.out.println("    [1]  Staff Portal   — Register found items, process claims");
        System.out.println("    [2]  Public Portal  — Report a lost item / check for match");
        System.out.println("    [3]  Admin Panel    — View records, statistics, search");
        System.out.println("    [0]  Exit");
        System.out.println("  " + DIVIDER);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STAFF MENU
    // ════════════════════════════════════════════════════════════════════════

    private void staffMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  " + DIVIDER);
            System.out.println("               STAFF PORTAL");
            System.out.println("  " + DIVIDER);
            System.out.println("    [1]  Register a Found Item");
            System.out.println("    [2]  Process a Claim (item pickup)");
            System.out.println("    [3]  View All Found Items");
            System.out.println("    [4]  Update Item Status");
            System.out.println("    [0]  Back to Main Menu");
            System.out.println("  " + DIVIDER);

            int choice = input.readInt("  Enter choice: ", 0, 4);
            switch (choice) {
                case 1 -> registerFoundItemFlow();
                case 2 -> processClaimFlow();
                case 3 -> listFoundItems();
                case 4 -> updateStatusFlow();
                case 0 -> back = true;
            }
        }
    }

    private void registerFoundItemFlow() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("          REGISTER FOUND ITEM");
        System.out.println("  " + DIVIDER);

        // Finder / staff info
        System.out.println("\n  --- Finder / Staff Details ---");
        String staffId   = "STF-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String staffName = input.readRequired("Your name (staff/finder): ");
        String staffPhone= input.readRequired("Your contact number      : ");
        Person finder    = new Person(staffId, staffName, staffPhone, null, null);

        // Item details
        System.out.println("\n  --- Item Details ---");
        String itemName  = input.readRequired("Item name                : ");
        String desc      = input.readRequired("Description              : ");
        Category cat     = input.readCategory();
        String color     = input.readOptional("Color (or Enter to skip) : ");
        String brand     = input.readOptional("Brand (or Enter to skip) : ");
        String imagePath = input.readImagePath("Photo of item");

        // Location where found
        System.out.println("\n  --- Location Found ---");
        String building  = input.readRequired("Building/Block           : ");
        String floor     = input.readOptional("Floor (e.g. Ground, 2nd) : ");
        String area      = input.readRequired("Area/Zone (e.g. Canteen) : ");
        String locNotes  = input.readOptional("Additional location notes: ");
        Location loc     = new Location(building, floor, area, locNotes);

        // Time
        System.out.println("\n  --- Time Found ---");
        LocalDateTime foundTime = input.readDateTime("When was it found?");
        if (foundTime == null) foundTime = LocalDateTime.now();

        String finderNotes   = input.readOptional("Finder notes (optional)  : ");
        String storageLoc    = input.readOptional("Storage location         : ");
        if (storageLoc.isBlank()) storageLoc = "Lost & Found Office, Room 101";

        FoundItem item = new FoundItem(
            itemName, desc, cat,
            color.isBlank() ? null : color,
            brand.isBlank() ? null : brand,
            imagePath, loc, finder,
            foundTime, finderNotes.isBlank() ? null : finderNotes,
            storageLoc
        );

        service.registerFoundItem(item);
        input.pause();
    }

    private void processClaimFlow() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("            PROCESS ITEM CLAIM");
        System.out.println("  " + DIVIDER);

        System.out.println("\n  Active found items (unclaimed):");
        List<FoundItem> active = service.getActiveFoundItems();
        if (active.isEmpty()) {
            System.out.println("  No active found items in the system.");
            input.pause();
            return;
        }
        printFoundItemTable(active);

        String itemId = input.readRequired("\n  Enter Found Item ID to claim: ").toUpperCase();

        var opt = service.getFoundItemById(itemId);
        if (opt.isEmpty()) {
            System.out.println("  ✗  Item ID not found: " + itemId);
            input.pause();
            return;
        }

        FoundItem item = opt.get();
        if (item.getStatus() == Status.CLAIMED) {
            System.out.println("  ✗  This item has already been claimed.");
            input.pause();
            return;
        }

        System.out.println("\n  Item to be claimed:");
        System.out.println("  " + item.getName() + " [" + item.getItemId() + "]");

        // Claimant details
        System.out.println("\n  --- Claimant Details ---");
        String claimId   = "CLM-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String cName     = input.readRequired("Claimant full name   : ");
        String cPhone    = input.readRequired("Contact number       : ");
        String cEmail    = input.readOptional("Email (optional)     : ");
        String cAddress  = input.readOptional("Address (optional)   : ");
        Person claimant  = new Person(claimId, cName, cPhone,
                cEmail.isBlank() ? null : cEmail,
                cAddress.isBlank() ? null : cAddress);

        String verifyNotes = input.readOptional("Verification notes   : ");
        String handledBy   = input.readRequired("Handled by (staff)   : ");

        service.processClaim(itemId, claimant, verifyNotes, handledBy);
        input.pause();
    }

    private void listFoundItems() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("            ALL FOUND ITEMS");
        System.out.println("  " + DIVIDER);
        List<FoundItem> items = service.getAllFoundItems();
        if (items.isEmpty()) {
            System.out.println("  No found items registered yet.");
        } else {
            printFoundItemTable(items);
        }
        input.pause();
    }

    private void updateStatusFlow() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("            UPDATE ITEM STATUS");
        System.out.println("  " + DIVIDER);
        printFoundItemTable(service.getAllFoundItems());

        String id = input.readRequired("\n  Enter Found Item ID: ").toUpperCase();

        System.out.println("\n  Select new status:");
        Status[] statuses = Status.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.printf("    [%d] %s%n", i + 1, statuses[i].getDisplayName());
        }
        int s = input.readInt("  Choice: ", 1, statuses.length);
        service.updateFoundItemStatus(id, statuses[s - 1]);
        System.out.println("  ✓  Status updated to: " + statuses[s - 1].getDisplayName());
        input.pause();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PUBLIC CHECK-IN FLOW
    // ════════════════════════════════════════════════════════════════════════

    private void publicCheckInFlow() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("           PUBLIC — LOST ITEM CHECK-IN");
        System.out.println("  " + DIVIDER);
        System.out.println("  We'll try to match your lost item against our database.");
        System.out.println("  If found, you'll be directed to claim it.");
        System.out.println("  If not found, your report will be saved for future matching.\n");

        // Person info
        System.out.println("  --- Your Details ---");
        String pid    = "PUB-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String name   = input.readRequired("Your full name        : ");
        String phone  = input.readRequired("Contact number        : ");
        String email  = input.readOptional("Email (optional)      : ");
        String addr   = input.readOptional("Address (optional)    : ");
        Person person = new Person(pid, name, phone,
                email.isBlank() ? null : email,
                addr.isBlank() ? null : addr);

        // Lost item details
        System.out.println("\n  --- Lost Item Details ---");
        String itemName = input.readRequired("Item name             : ");
        String desc     = input.readRequired("Description           : ");
        Category cat    = input.readCategory();
        String color    = input.readOptional("Color (optional)      : ");
        String brand    = input.readOptional("Brand (optional)      : ");
        String imgPath  = input.readImagePath("Photo of item (optional)");

        // Where lost
        System.out.println("\n  --- Where Did You Lose It? ---");
        String building = input.readRequired("Building/Block        : ");
        String floor    = input.readOptional("Floor                 : ");
        String area     = input.readRequired("Area/Zone             : ");
        String locNote  = input.readOptional("Additional details    : ");
        Location loc    = new Location(building, floor, area, locNote);

        // When lost
        System.out.println("\n  --- When Did You Lose It? ---");
        System.out.println("  Provide a time range if unsure:");
        LocalDateTime lostStart = input.readDateTime("Earliest possible time");
        LocalDateTime lostEnd   = input.readDateTime("Latest possible time  ");

        String notes   = input.readOptional("\n  Any other details     : ");
        String reward  = input.readOptional("Reward offered        : ");

        LostItem lostItem = new LostItem(
            itemName, desc, cat,
            color.isBlank()  ? null : color,
            brand.isBlank()  ? null : brand,
            imgPath, loc, person,
            lostStart, lostEnd,
            notes.isBlank()  ? null : notes,
            reward.isBlank() ? null : reward
        );

        System.out.println("\n  " + DIVIDER2);
        System.out.println("  🔍  Searching database for matches...");
        System.out.println("  " + DIVIDER2);

        List<MatchResult> matches = service.checkInForLostItem(lostItem);

        if (!matches.isEmpty()) {
            System.out.println("\n  Match Report Details:");
            for (MatchResult mr : matches) {
                System.out.println(mr.getMatchReport());
            }

            if (input.readYesNo("\n  Would you like to come in and claim the matched item?")) {
                MatchResult best = matches.get(0);
                System.out.println("\n  Please visit: Lost & Found Office, Room 101");
                System.out.println("  Bring your ID and reference number: " + lostItem.getItemId());
                System.out.println("  Item waiting for you: " + best.getFoundItem().getName() +
                                   " [" + best.getFoundItem().getItemId() + "]");
                System.out.println("  Storage location: " +
                                   (best.getFoundItem().getStorageLocation() != null ?
                                    best.getFoundItem().getStorageLocation() : "Main Office"));
            }
        }

        System.out.println("\n  Your Reference ID: " + lostItem.getItemId());
        System.out.println("  Save this for future follow-ups.");
        input.pause();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ADMIN MENU
    // ════════════════════════════════════════════════════════════════════════

    private void adminMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  " + DIVIDER);
            System.out.println("                ADMIN PANEL");
            System.out.println("  " + DIVIDER);
            System.out.println("    [1]  View System Statistics");
            System.out.println("    [2]  View All Lost Reports");
            System.out.println("    [3]  View All Claim Records");
            System.out.println("    [4]  Search Items (keyword)");
            System.out.println("    [5]  View Item Full Details (by ID)");
            System.out.println("    [6]  View Unmatched Lost Reports");
            System.out.println("    [7]  Run Manual Match Check");
            System.out.println("    [0]  Back to Main Menu");
            System.out.println("  " + DIVIDER);

            int choice = input.readInt("  Enter choice: ", 0, 7);
            switch (choice) {
                case 1 -> service.getStats().print();
                case 2 -> listLostItems();
                case 3 -> listClaimRecords();
                case 4 -> searchFlow();
                case 5 -> viewItemDetails();
                case 6 -> listUnmatchedLostItems();
                case 7 -> manualMatchFlow();
                case 0 -> back = true;
            }
            if (choice != 0) input.pause();
        }
    }

    private void listLostItems() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("           ALL LOST ITEM REPORTS");
        System.out.println("  " + DIVIDER);
        List<LostItem> items = service.getAllLostItems();
        if (items.isEmpty()) {
            System.out.println("  No lost item reports yet.");
            return;
        }
        System.out.printf("  %-8s %-20s %-15s %-12s %-10s%n",
                "ID", "Name", "Category", "Status", "Reported");
        System.out.println("  " + DIVIDER2);
        for (LostItem i : items) {
            System.out.printf("  %-8s %-20s %-15s %-12s %-10s%n",
                    i.getItemId(),
                    truncate(i.getName(), 19),
                    truncate(i.getCategory().toString(), 14),
                    truncate(i.getStatus().toString(), 11),
                    i.getReportedTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        }
        System.out.println("  Total: " + items.size() + " records");
    }

    private void listClaimRecords() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("             CLAIM RECORDS");
        System.out.println("  " + DIVIDER);
        List<ClaimRecord> records = service.getAllClaimRecords();
        if (records.isEmpty()) {
            System.out.println("  No claims processed yet.");
            return;
        }
        System.out.printf("  %-12s %-10s %-18s %-16s%n",
                "Claim ID", "Item ID", "Claimant", "Date");
        System.out.println("  " + DIVIDER2);
        for (ClaimRecord r : records) {
            System.out.printf("  %-12s %-10s %-18s %-16s%n",
                    r.getClaimId(),
                    r.getFoundItem().getItemId(),
                    truncate(r.getClaimant().getName(), 17),
                    r.getClaimTime().format(FMT));
        }
        System.out.println("  Total: " + records.size() + " claims");
    }

    private void searchFlow() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("              KEYWORD SEARCH");
        System.out.println("  " + DIVIDER);
        String kw = input.readRequired("Enter keyword to search: ");

        System.out.println("\n  --- Matching Found Items ---");
        List<FoundItem> found = service.searchFoundItems(kw);
        if (found.isEmpty()) System.out.println("  No found items match \"" + kw + "\".");
        else printFoundItemTable(found);

        System.out.println("\n  --- Matching Lost Reports ---");
        List<LostItem> lost = service.searchLostItems(kw);
        if (lost.isEmpty()) System.out.println("  No lost reports match \"" + kw + "\".");
        else {
            for (LostItem i : lost) {
                System.out.printf("  [%s] %s — %s — %s%n",
                        i.getItemId(), i.getName(), i.getCategory(), i.getStatus());
            }
        }
    }

    private void viewItemDetails() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("  Enter item ID (Found or Lost):   ");
        String id = input.readRequired("> ").toUpperCase();

        var foundOpt = service.getFoundItemById(id);
        if (foundOpt.isPresent()) {
            System.out.println(foundOpt.get().getSummary());
            return;
        }
        var lostOpt = service.getLostItemById(id);
        if (lostOpt.isPresent()) {
            System.out.println(lostOpt.get().getSummary());
            return;
        }
        System.out.println("  ✗  No item found with ID: " + id);
    }

    private void listUnmatchedLostItems() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("        UNMATCHED LOST ITEM REPORTS");
        System.out.println("  " + DIVIDER);
        List<LostItem> items = service.getUnmatchedLostItems();
        if (items.isEmpty()) {
            System.out.println("  All lost reports have been matched or there are none.");
            return;
        }
        for (LostItem i : items) {
            System.out.printf("  [%s] %-20s %-14s Reporter: %s (%s)%n",
                    i.getItemId(),
                    truncate(i.getName(), 19),
                    i.getCategory(),
                    i.getReportedBy().getName(),
                    i.getReportedBy().getContactNumber());
        }
        System.out.println("  Total unmatched: " + items.size());
    }

    private void manualMatchFlow() {
        System.out.println("\n  " + DIVIDER);
        System.out.println("           MANUAL MATCH CHECK");
        System.out.println("  " + DIVIDER);
        System.out.println("  This will re-run matching for a specific lost report.");

        listUnmatchedLostItems();
        if (service.getUnmatchedLostItems().isEmpty()) return;

        String id = input.readRequired("\n  Enter Lost Item ID to re-match: ").toUpperCase();
        var opt = service.getLostItemById(id);
        if (opt.isEmpty()) {
            System.out.println("  ✗  Lost item not found: " + id);
            return;
        }

        LostItem lost = opt.get();
        System.out.println("  🔍  Running match for: " + lost.getName() + " [" + id + "]");

        List<MatchResult> matches = service.checkInForLostItem(lost);
        if (matches.isEmpty()) {
            System.out.println("  No matches found after re-check.");
        } else {
            System.out.println("  Found " + matches.size() + " match(es):");
            for (MatchResult mr : matches) {
                System.out.println(mr.getMatchReport());
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════════

    private void printFoundItemTable(List<FoundItem> items) {
        System.out.printf("  %-8s %-20s %-14s %-12s %-10s %-8s%n",
                "ID", "Name", "Category", "Status", "Found On", "Color");
        System.out.println("  " + DIVIDER2);
        for (FoundItem i : items) {
            System.out.printf("  %-8s %-20s %-14s %-12s %-10s %-8s%n",
                    i.getItemId(),
                    truncate(i.getName(), 19),
                    truncate(i.getCategory().toString(), 13),
                    truncate(i.getStatus().toString(), 11),
                    i.getFoundTime() != null ?
                        i.getFoundTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "N/A",
                    i.getColor() != null ? truncate(i.getColor(), 7) : "N/A");
        }
        System.out.println("  Total: " + items.size() + " item(s)");
    }

    private String truncate(String s, int len) {
        if (s == null) return "";
        return s.length() <= len ? s : s.substring(0, len - 1) + "…";
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
