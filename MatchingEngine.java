package com.lostandfound.service;

import com.lostandfound.model.FoundItem;
import com.lostandfound.model.LostItem;
import com.lostandfound.model.Location;
import com.lostandfound.model.MatchResult;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Core matching engine that compares a LostItem against FoundItems.
 *
 * Scoring weights (must sum to 1.0):
 *   Category   : 0.25  — wrong category = near-impossible match
 *   Name/Desc  : 0.25  — keyword overlap
 *   Color      : 0.15  — visual attribute
 *   Brand      : 0.10  — brand match
 *   Location   : 0.15  — proximity of lost vs. found location
 *   Time       : 0.10  — time window plausibility
 */
public class MatchingEngine {

    // ── Weights ─────────────────────────────────────────────────────────────
    private static final double W_CATEGORY = 0.25;
    private static final double W_NAME_DESC = 0.25;
    private static final double W_COLOR = 0.15;
    private static final double W_BRAND = 0.10;
    private static final double W_LOCATION = 0.15;
    private static final double W_TIME = 0.10;

    // Time window: if the lost time range and found time are within this many hours, full score
    private static final long TIME_WINDOW_HOURS = 72;

    /**
     * Matches a single LostItem against all provided FoundItems.
     * Returns a sorted list of MatchResults (best first).
     */
    public List<MatchResult> match(LostItem lostItem, List<FoundItem> foundItems) {
        List<MatchResult> results = new ArrayList<>();

        for (FoundItem found : foundItems) {
            MatchResult result = computeMatch(lostItem, found);
            if (result.isMatch()) {
                results.add(result);
            }
        }

        Collections.sort(results);
        return results;
    }

    /**
     * Computes a detailed MatchResult between one LostItem and one FoundItem.
     */
    public MatchResult computeMatch(LostItem lost, FoundItem found) {
        Map<String, Double> breakdown = new LinkedHashMap<>();

        double catScore   = scoreCategory(lost, found);
        double nameScore  = scoreNameDescription(lost, found);
        double colorScore = scoreColor(lost, found);
        double brandScore = scoreBrand(lost, found);
        double locScore   = scoreLocation(lost, found);
        double timeScore  = scoreTime(lost, found);

        breakdown.put("Category",    catScore);
        breakdown.put("Name/Desc",   nameScore);
        breakdown.put("Color",       colorScore);
        breakdown.put("Brand",       brandScore);
        breakdown.put("Location",    locScore);
        breakdown.put("Time",        timeScore);

        double overall = W_CATEGORY * catScore
                       + W_NAME_DESC * nameScore
                       + W_COLOR * colorScore
                       + W_BRAND * brandScore
                       + W_LOCATION * locScore
                       + W_TIME * timeScore;

        return new MatchResult(lost, found, overall, breakdown);
    }

    // ── Individual scorers ───────────────────────────────────────────────────

    private double scoreCategory(LostItem lost, FoundItem found) {
        if (lost.getCategory() == null || found.getCategory() == null) return 0.3;
        return lost.getCategory() == found.getCategory() ? 1.0 : 0.0;
    }

    private double scoreNameDescription(LostItem lost, FoundItem found) {
        double score = 0.0;
        int checks = 0;

        // Name keyword overlap
        if (lost.getName() != null && found.getName() != null) {
            checks++;
            score += keywordOverlap(lost.getName(), found.getName());
        }

        // Description keyword overlap
        if (lost.getDescription() != null && found.getDescription() != null) {
            checks++;
            score += keywordOverlap(lost.getDescription(), found.getDescription()) * 0.8;
        }

        // Cross-check: lost name in found description and vice versa
        if (lost.getName() != null && found.getDescription() != null) {
            checks++;
            score += keywordOverlap(lost.getName(), found.getDescription()) * 0.6;
        }

        return checks == 0 ? 0.2 : Math.min(1.0, score / checks);
    }

    private double scoreColor(LostItem lost, FoundItem found) {
        if (lost.getColor() == null || found.getColor() == null) return 0.3;
        String lc = lost.getColor().toLowerCase().trim();
        String fc = found.getColor().toLowerCase().trim();
        if (lc.equals(fc)) return 1.0;
        if (lc.contains(fc) || fc.contains(lc)) return 0.7;
        return 0.0;
    }

    private double scoreBrand(LostItem lost, FoundItem found) {
        if (lost.getBrand() == null || found.getBrand() == null) return 0.3;
        String lb = lost.getBrand().toLowerCase().trim();
        String fb = found.getBrand().toLowerCase().trim();
        if (lb.equals(fb)) return 1.0;
        if (lb.contains(fb) || fb.contains(lb)) return 0.6;
        return 0.0;
    }

    private double scoreLocation(LostItem lost, FoundItem found) {
        Location lostLoc  = lost.getSuspectedLostLocation();
        Location foundLoc = found.getFoundLocation() != null ? found.getFoundLocation() : found.getLocation();
        if (lostLoc == null || foundLoc == null) return 0.3;
        return lostLoc.similarityScore(foundLoc);
    }

    private double scoreTime(LostItem lost, FoundItem found) {
        LocalDateTime lostStart = lost.getSuspectedLostTime();
        LocalDateTime lostEnd   = lost.getSuspectedLostTimeEnd();
        LocalDateTime foundTime = found.getFoundTime();

        if (lostStart == null || foundTime == null) return 0.3;

        // Item must have been found AFTER it was lost
        if (foundTime.isBefore(lostStart)) {
            // Allow small margin (24h) for uncertainty
            long hoursBefore = ChronoUnit.HOURS.between(foundTime, lostStart);
            if (hoursBefore > 24) return 0.0;
            return 0.2;
        }

        // If a time range is provided, check if foundTime falls within it (with buffer)
        LocalDateTime windowEnd = (lostEnd != null ? lostEnd : lostStart).plusHours(TIME_WINDOW_HOURS);
        if (!foundTime.isAfter(windowEnd)) {
            long hours = ChronoUnit.HOURS.between(lostStart, foundTime);
            if (hours <= 24) return 1.0;
            if (hours <= 48) return 0.8;
            if (hours <= TIME_WINDOW_HOURS) return 0.6;
        }

        // Found much later — plausibility declines
        long daysLater = ChronoUnit.DAYS.between(lostStart, foundTime);
        if (daysLater <= 7)  return 0.4;
        if (daysLater <= 30) return 0.2;
        return 0.1;
    }

    // ── Utility ─────────────────────────────────────────────────────────────

    /**
     * Computes keyword overlap ratio between two strings.
     */
    private double keywordOverlap(String a, String b) {
        Set<String> wordsA = tokenize(a);
        Set<String> wordsB = tokenize(b);
        if (wordsA.isEmpty() || wordsB.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(wordsA);
        intersection.retainAll(wordsB);

        Set<String> union = new HashSet<>(wordsA);
        union.addAll(wordsB);

        return (double) intersection.size() / union.size(); // Jaccard similarity
    }

    private Set<String> tokenize(String text) {
        if (text == null) return Collections.emptySet();
        Set<String> stopWords = Set.of("a", "an", "the", "is", "in", "at", "on", "of", "my", "it");
        Set<String> tokens = new HashSet<>();
        for (String word : text.toLowerCase().split("[\\s,./\\-_]+")) {
            if (word.length() > 1 && !stopWords.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }
}
