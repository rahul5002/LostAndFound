package com.lostandfound.enums;

public enum Category {
    ELECTRONICS("Electronics"),
    CLOTHING("Clothing & Accessories"),
    DOCUMENTS("Documents & Cards"),
    JEWELRY("Jewelry & Watches"),
    BAGS("Bags & Luggage"),
    KEYS("Keys"),
    BOOKS("Books & Stationery"),
    SPORTS("Sports Equipment"),
    TOYS("Toys & Games"),
    MEDICAL("Medical Devices"),
    MONEY("Money & Wallets"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
