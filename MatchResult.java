package com.lostandfound.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulates the result of matching a LostItem against a FoundItem.
 * Holds the score breakdown and overall confidence level.
 */
public class MatchResult implements Comparable<MatchResult> {

    public enum ConfidenceLevel {
        HIGH("HIGH - Strong Match", "★★★"),
        MEDIUM("MEDIUM - Possible Match", "★★☆"),
        LOW("LOW - Weak Match", "★☆☆"),
        NO_MATCH("NO MATCH", "☆☆☆");

        private final String label;
        private final String stars;

        ConfidenceLevel(String label, String stars) {
            this.label = label;
            this.stars = stars;
        }

        public String getLabel() { return label; }
        public String getStars() { return stars; }
    }

    private final LostItem lostItem;
    private final FoundItem foundItem;
    private final double overallScore;           // 0.0 to 1.0
    private final Map<String, Double> scoreBreakdown;
    private final ConfidenceLevel confidenceLevel;
    private final LocalDateTime matchedAt;

    public MatchResult(LostItem lostItem, FoundItem foundItem,
                       double overallScore, Map<String, Double> scoreBreakdown) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.overallScore = overallScore;
        this.scoreBreakdown = new LinkedHashMap<>(scoreBreakdown);
        this.matchedAt = LocalDateTime.now();
        this.confidenceLevel = determineConfidence(overallScore);
    }

    private ConfidenceLevel determineConfidence(double score) {
        if (score >= 0.70) return ConfidenceLevel.HIGH;
        if (score >= 0.45) return ConfidenceLevel.MEDIUM;
        if (score >= 0.25) return ConfidenceLevel.LOW;
        return ConfidenceLevel.NO_MATCH;
    }

    public boolean isMatch() {
        return confidenceLevel != ConfidenceLevel.NO_MATCH;
    }

    public boolean isStrongMatch() {
        return confidenceLevel == ConfidenceLevel.HIGH;
    }

    public String getMatchReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════════════╗\n");
        sb.append("║             MATCH ANALYSIS REPORT                   ║\n");
        sb.append("╠══════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  Lost Item ID  : %-34s ║\n", lostItem.getItemId()));
        sb.append(String.format("║  Found Item ID : %-34s ║\n", foundItem.getItemId()));
        sb.append(String.format("║  Overall Score : %-6.1f%%                              ║\n", overallScore * 100));
        sb.append(String.format("║  Confidence    : %-34s ║\n",
                confidenceLevel.getStars() + " " + confidenceLevel.getLabel()));
        sb.append("╠══════════════════════════════════════════════════════╣\n");
        sb.append("║  SCORE BREAKDOWN:                                    ║\n");

        for (Map.Entry<String, Double> entry : scoreBreakdown.entrySet()) {
            String bar = buildBar(entry.getValue());
            sb.append(String.format("║  %-15s: %s %-5.1f%%          ║\n",
                    entry.getKey(), bar, entry.getValue() * 100));
        }

        sb.append("╚══════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }

    private String buildBar(double value) {
        int filled = (int) (value * 10);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 10; i++) bar.append(i < filled ? "█" : "░");
        bar.append("]");
        return bar.toString();
    }

    // Getters
    public LostItem getLostItem() { return lostItem; }
    public FoundItem getFoundItem() { return foundItem; }
    public double getOverallScore() { return overallScore; }
    public Map<String, Double> getScoreBreakdown() { return scoreBreakdown; }
    public ConfidenceLevel getConfidenceLevel() { return confidenceLevel; }
    public LocalDateTime getMatchedAt() { return matchedAt; }

    @Override
    public int compareTo(MatchResult other) {
        return Double.compare(other.overallScore, this.overallScore); // descending
    }

    @Override
    public String toString() {
        return String.format("MatchResult[Lost:%s <-> Found:%s Score:%.1f%% Confidence:%s]",
                lostItem.getItemId(), foundItem.getItemId(),
                overallScore * 100, confidenceLevel.getLabel());
    }
}
