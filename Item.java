package com.lostandfound.model;

import com.lostandfound.enums.Category;
import com.lostandfound.enums.Status;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class representing a generic item in the Lost and Found system.
 * All item types (FoundItem, LostItem) inherit from this class.
 */
public abstract class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String itemId;
    protected String name;
    protected String description;
    protected Category category;
    protected String color;
    protected String brand;
    protected String imagePath;        // Path to image file on disk
    protected Location location;
    protected LocalDateTime reportedTime;
    protected Status status;
    protected Person reportedBy;

    public Item() {
        this.itemId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.reportedTime = LocalDateTime.now();
        this.status = Status.FOUND;
    }

    public Item(String name, String description, Category category,
                String color, String brand, String imagePath,
                Location location, Person reportedBy) {
        this();
        this.name = name;
        this.description = description;
        this.category = category;
        this.color = color;
        this.brand = brand;
        this.imagePath = imagePath;
        this.location = location;
        this.reportedBy = reportedBy;
    }

    // Abstract method: each subtype defines its display summary
    public abstract String getSummary();

    // Getters and Setters
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public LocalDateTime getReportedTime() { return reportedTime; }
    public void setReportedTime(LocalDateTime reportedTime) { this.reportedTime = reportedTime; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Person getReportedBy() { return reportedBy; }
    public void setReportedBy(Person reportedBy) { this.reportedBy = reportedBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equals(itemId, item.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }
}
