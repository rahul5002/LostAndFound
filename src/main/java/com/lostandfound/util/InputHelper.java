package com.lostandfound.util;

import com.lostandfound.enums.Category;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Utility class for safe, validated console input reading.
 */
public class InputHelper {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Reads a non-blank string, re-prompting if empty. */
    public String readRequired(String prompt) {
        while (true) {
            System.out.print("  " + prompt);
            String val = scanner.nextLine().trim();
            if (!val.isEmpty()) return val;
            System.out.println("  ⚠  This field is required. Please enter a value.");
        }
    }

    /** Reads an optional string (may be blank). */
    public String readOptional(String prompt) {
        System.out.print("  " + prompt);
        return scanner.nextLine().trim();
    }

    /** Reads an integer within [min, max]. */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print("  " + prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                System.out.printf("  ⚠  Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Invalid input. Please enter a number.");
            }
        }
    }

    /** Reads a yes/no answer, returns true for 'y'. */
    public boolean readYesNo(String prompt) {
        while (true) {
            System.out.print("  " + prompt + " (y/n): ");
            String val = scanner.nextLine().trim().toLowerCase();
            if (val.equals("y") || val.equals("yes")) return true;
            if (val.equals("n") || val.equals("no"))  return false;
            System.out.println("  ⚠  Please enter 'y' or 'n'.");
        }
    }

    /**
     * Reads a datetime in dd-MM-yyyy HH:mm format.
     * Returns null if the user skips (presses Enter).
     */
    public LocalDateTime readDateTime(String prompt) {
        System.out.println("  " + prompt + " (format: dd-MM-yyyy HH:mm, or press Enter to skip)");
        while (true) {
            System.out.print("  > ");
            String val = scanner.nextLine().trim();
            if (val.isEmpty()) return null;
            try {
                return LocalDateTime.parse(val, FMT);
            } catch (DateTimeParseException e) {
                System.out.println("  ⚠  Invalid format. Use dd-MM-yyyy HH:mm  e.g. 25-04-2025 14:30");
            }
        }
    }

    /** Displays a numbered category menu and returns the chosen Category. */
    public Category readCategory() {
        Category[] cats = Category.values();
        System.out.println("\n  Select Category:");
        for (int i = 0; i < cats.length; i++) {
            System.out.printf("    [%2d] %s%n", i + 1, cats[i].getDisplayName());
        }
        int choice = readInt("  Enter choice (1-" + cats.length + "): ", 1, cats.length);
        return cats[choice - 1];
    }

    /** Reads an optional image file path. */
    public String readImagePath(String prompt) {
        System.out.println("  " + prompt);
        System.out.println("  (Enter full file path, or press Enter to skip)");
        System.out.print("  > ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) return null;
        java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            System.out.println("  ⚠  File not found. Path saved as reference only.");
        }
        return path.isEmpty() ? null : path;
    }

    public void pause() {
        System.out.print("\n  Press Enter to continue...");
        scanner.nextLine();
    }
}
