package com.lostandfound.model;

import com.lostandfound.enums.Category;
import com.lostandfound.enums.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an item that has been found and submitted to the Lost & Found department.
 * Contains details about where and when the item was discovered.
 */
public class FoundItem extends Item {
    private static final long serialVersionUID = 1L;

    private LocalDateTime foundTime;         // When the item was physically found
    private Location foundLocation;          // Where it was found (may differ from reported location)
    private String finderNotes;              // Any additional notes from the finder
    private String storageLocation;          // Where the item is currently being kept
    private LocalDateTime expiryDate;        // After this date, item may be disposed of

    public FoundItem() {
        super();
        this.status = Status.FOUND;
        this.foundTime = LocalDateTime.now();
        this.expiryDate = LocalDateTime.now().plusDays(90); // 90-day default holding period
    }

    public FoundItem(String name, String description, Category category,
                     String color, String brand, String imagePath,
                     Location foundLocation, Person reportedBy,
                     LocalDateTime foundTime, String finderNotes, String storageLocation) {
        super(name, description, category, color, brand, imagePath, foundLocation, reportedBy);
        this.foundTime = (foundTime != null) ? foundTime : LocalDateTime.now();
        this.foundLocation = foundLocation;
        this.finderNotes = finderNotes;
        this.storageLocation = storageLocation;
        this.status = Status.FOUND;
        this.expiryDate = this.foundTime.plusDays(90);
    }

    @Override
    public String getSummary() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return String.format(
            "╔══════════════════════════════════════════════════╗\n" +
            "  [FOUND ITEM] ID: %s\n" +
            "  Name       : %s\n" +
            "  Category   : %s\n" +
            "  Color      : %s   Brand: %s\n" +
            "  Found At   : %s\n" +
            "  Found On   : %s\n" +
            "  Storage    : %s\n" +
            "  Status     : %s\n" +
            "  Image      : %s\n" +
            "  Notes      : %s\n" +
            "╚══════════════════════════════════════════════════╝",
            itemId, name, category,
            (color != null ? color : "N/A"), (brand != null ? brand : "N/A"),
            (foundLocation != null ? foundLocation : "Unknown"),
            foundTime.format(fmt),
            (storageLocation != null ? storageLocation : "Main Office"),
            status,
            (imagePath != null ? imagePath : "No image"),
            (finderNotes != null ? finderNotes : "None")
        );
    }

    // Getters and Setters
    public LocalDateTime getFoundTime() { return foundTime; }
    public void setFoundTime(LocalDateTime foundTime) { this.foundTime = foundTime; }

    public Location getFoundLocation() { return foundLocation; }
    public void setFoundLocation(Location foundLocation) {
        this.foundLocation = foundLocation;
        this.location = foundLocation;
    }

    public String getFinderNotes() { return finderNotes; }
    public void setFinderNotes(String finderNotes) { this.finderNotes = finderNotes; }

    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    @Override
    public String toString() {
        return String.format("FoundItem[%s] %s - %s @ %s", itemId, name, category,
                foundLocation != null ? foundLocation.toString() : "Unknown");
    }
}
