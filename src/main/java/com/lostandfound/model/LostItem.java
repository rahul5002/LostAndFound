package com.lostandfound.model;

import com.lostandfound.enums.Category;
import com.lostandfound.enums.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an item that has been reported as lost by its owner.
 * The owner provides details to help match against found items in the database.
 */
public class LostItem extends Item {
    private static final long serialVersionUID = 1L;

    private LocalDateTime suspectedLostTime;         // When the owner thinks they lost it
    private LocalDateTime suspectedLostTimeEnd;      // For time range (e.g., "between 2pm and 5pm")
    private Location suspectedLostLocation;          // Where the owner thinks they lost it
    private String ownerNotes;                       // Additional context from owner
    private String rewardOffered;                    // Optional reward description
    private String matchedFoundItemId;               // ID of matched FoundItem (if any)

    public LostItem() {
        super();
        this.status = Status.LOST;
    }

    public LostItem(String name, String description, Category category,
                    String color, String brand, String imagePath,
                    Location suspectedLostLocation, Person owner,
                    LocalDateTime suspectedLostTime, LocalDateTime suspectedLostTimeEnd,
                    String ownerNotes, String rewardOffered) {
        super(name, description, category, color, brand, imagePath, suspectedLostLocation, owner);
        this.suspectedLostTime = suspectedLostTime;
        this.suspectedLostTimeEnd = suspectedLostTimeEnd;
        this.suspectedLostLocation = suspectedLostLocation;
        this.ownerNotes = ownerNotes;
        this.rewardOffered = rewardOffered;
        this.status = Status.LOST;
    }

    @Override
    public String getSummary() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String timeRange = (suspectedLostTime != null ? suspectedLostTime.format(fmt) : "Unknown");
        if (suspectedLostTimeEnd != null) {
            timeRange += " to " + suspectedLostTimeEnd.format(fmt);
        }

        return String.format(
            "╔══════════════════════════════════════════════════╗\n" +
            "  [LOST ITEM] ID: %s\n" +
            "  Name       : %s\n" +
            "  Category   : %s\n" +
            "  Color      : %s   Brand: %s\n" +
            "  Lost Near  : %s\n" +
            "  Lost When  : %s\n" +
            "  Status     : %s\n" +
            "  Image      : %s\n" +
            "  Reward     : %s\n" +
            "  Notes      : %s\n" +
            "╚══════════════════════════════════════════════════╝",
            itemId, name, category,
            (color != null ? color : "N/A"), (brand != null ? brand : "N/A"),
            (suspectedLostLocation != null ? suspectedLostLocation : "Unknown"),
            timeRange, status,
            (imagePath != null ? imagePath : "No image"),
            (rewardOffered != null && !rewardOffered.isBlank() ? rewardOffered : "None"),
            (ownerNotes != null ? ownerNotes : "None")
        );
    }

    // Getters and Setters
    public LocalDateTime getSuspectedLostTime() { return suspectedLostTime; }
    public void setSuspectedLostTime(LocalDateTime suspectedLostTime) { this.suspectedLostTime = suspectedLostTime; }

    public LocalDateTime getSuspectedLostTimeEnd() { return suspectedLostTimeEnd; }
    public void setSuspectedLostTimeEnd(LocalDateTime end) { this.suspectedLostTimeEnd = end; }

    public Location getSuspectedLostLocation() { return suspectedLostLocation; }
    public void setSuspectedLostLocation(Location location) {
        this.suspectedLostLocation = location;
        this.location = location;
    }

    public String getOwnerNotes() { return ownerNotes; }
    public void setOwnerNotes(String ownerNotes) { this.ownerNotes = ownerNotes; }

    public String getRewardOffered() { return rewardOffered; }
    public void setRewardOffered(String rewardOffered) { this.rewardOffered = rewardOffered; }

    public String getMatchedFoundItemId() { return matchedFoundItemId; }
    public void setMatchedFoundItemId(String matchedFoundItemId) { this.matchedFoundItemId = matchedFoundItemId; }

    @Override
    public String toString() {
        return String.format("LostItem[%s] %s - %s @ %s", itemId, name, category,
                suspectedLostLocation != null ? suspectedLostLocation.toString() : "Unknown");
    }
}
