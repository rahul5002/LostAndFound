package com.lostandfound.model;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {
    private static final long serialVersionUID = 1L;

    private String building;
    private String floor;
    private String area;
    private String additionalDetails;

    public Location() {}

    public Location(String building, String floor, String area, String additionalDetails) {
        this.building = building;
        this.floor = floor;
        this.area = area;
        this.additionalDetails = additionalDetails;
    }

    // Getters and Setters
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getAdditionalDetails() { return additionalDetails; }
    public void setAdditionalDetails(String additionalDetails) { this.additionalDetails = additionalDetails; }

    /**
     * Calculates a location similarity score (0.0 to 1.0) between this location and another.
     */
    public double similarityScore(Location other) {
        if (other == null) return 0.0;
        double score = 0.0;
        int checks = 0;

        if (building != null && other.building != null) {
            checks++;
            if (building.equalsIgnoreCase(other.building)) score += 1.0;
            else if (building.toLowerCase().contains(other.building.toLowerCase()) ||
                     other.building.toLowerCase().contains(building.toLowerCase())) score += 0.5;
        }
        if (floor != null && other.floor != null) {
            checks++;
            if (floor.equalsIgnoreCase(other.floor)) score += 1.0;
        }
        if (area != null && other.area != null) {
            checks++;
            if (area.equalsIgnoreCase(other.area)) score += 1.0;
            else if (area.toLowerCase().contains(other.area.toLowerCase()) ||
                     other.area.toLowerCase().contains(area.toLowerCase())) score += 0.6;
        }

        return checks == 0 ? 0.0 : score / checks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (building != null && !building.isBlank()) sb.append(building);
        if (floor != null && !floor.isBlank()) sb.append(", Floor ").append(floor);
        if (area != null && !area.isBlank()) sb.append(", ").append(area);
        if (additionalDetails != null && !additionalDetails.isBlank()) sb.append(" (").append(additionalDetails).append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return Objects.equals(building, location.building) &&
               Objects.equals(floor, location.floor) &&
               Objects.equals(area, location.area);
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, floor, area);
    }
}
