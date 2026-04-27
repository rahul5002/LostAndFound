package com.lostandfound.service;

import com.lostandfound.enums.Status;
import com.lostandfound.model.*;
import com.lostandfound.repository.*;

import java.util.List;
import java.util.Optional;

/**
 * Central service orchestrating all Lost & Found operations.
 * Coordinates repositories, matching engine, and notifications.
 */
public class LostAndFoundService {

    private final FoundItemRepository foundRepo;
    private final LostItemRepository lostRepo;
    private final ClaimRecordRepository claimRepo;
    private final MatchingEngine matchingEngine;
    private final NotificationService notificationService;

    public LostAndFoundService() {
        this.foundRepo          = new FoundItemRepository();
        this.lostRepo           = new LostItemRepository();
        this.claimRepo          = new ClaimRecordRepository();
        this.matchingEngine     = new MatchingEngine();
        this.notificationService = new NotificationService();
    }

    // ── Register a found item ────────────────────────────────────────────────

    /**
     * Registers a newly found item in the system.
     * Also checks existing lost reports for a possible immediate match.
     */
    public FoundItem registerFoundItem(FoundItem item) {
        foundRepo.save(item);
        notificationService.notifyItemRegistered(item);

        // Reverse match: check if any existing lost report matches this new found item
        List<LostItem> lostItems = lostRepo.findUnmatched();
        for (LostItem lost : lostItems) {
            MatchResult result = matchingEngine.computeMatch(lost, item);
            if (result.isStrongMatch()) {
                notificationService.notifyAutoMatchFound(lost, item, result.getOverallScore());
            }
        }

        return item;
    }

    // ── Person checks in for their lost item ────────────────────────────────

    /**
     * Main check-in workflow:
     * 1. Match LostItem against found items database.
     * 2. If match(es) found → notify person.
     * 3. Otherwise → register the lost report for future matching.
     *
     * @return list of matches (empty if none found)
     */
    public List<MatchResult> checkInForLostItem(LostItem lostItem) {
        List<FoundItem> activeFoundItems = foundRepo.findActiveItems();
        List<MatchResult> matches = matchingEngine.match(lostItem, activeFoundItems);

        if (!matches.isEmpty()) {
            // Notify the person about potential matches
            notificationService.notifyMatchFound(lostItem.getReportedBy(), matches);

            // Mark top match as "matched" status
            MatchResult best = matches.get(0);
            if (best.isStrongMatch()) {
                FoundItem foundItem = best.getFoundItem();
                foundItem.setStatus(Status.MATCHED);
                foundRepo.update(foundItem);

                lostItem.setStatus(Status.MATCHED);
                lostItem.setMatchedFoundItemId(foundItem.getItemId());
            }

            lostRepo.save(lostItem);
        } else {
            // No match → register the lost report
            lostRepo.save(lostItem);
            notificationService.notifyNoMatchFound(lostItem.getReportedBy(), lostItem);
        }

        return matches;
    }

    // ── Claim an item ────────────────────────────────────────────────────────

    /**
     * Processes a physical claim when a person comes to collect their item.
     */
    public ClaimRecord processClaim(String foundItemId, Person claimant,
                                    String verificationNotes, String handledBy) {
        Optional<FoundItem> opt = foundRepo.findById(foundItemId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Found item not found: " + foundItemId);
        }

        FoundItem item = opt.get();
        if (item.getStatus() == Status.CLAIMED) {
            throw new IllegalStateException("Item " + foundItemId + " is already claimed.");
        }

        item.setStatus(Status.CLAIMED);
        foundRepo.update(item);

        ClaimRecord record = new ClaimRecord(item, claimant, verificationNotes, handledBy);
        claimRepo.save(record);
        notificationService.notifyClaimProcessed(record);

        return record;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<FoundItem> getAllFoundItems()          { return foundRepo.findAll(); }
    public List<LostItem>  getAllLostItems()           { return lostRepo.findAll(); }
    public List<ClaimRecord> getAllClaimRecords()      { return claimRepo.findAll(); }
    public List<FoundItem> getActiveFoundItems()       { return foundRepo.findActiveItems(); }
    public List<LostItem> getUnmatchedLostItems()      { return lostRepo.findUnmatched(); }

    public Optional<FoundItem> getFoundItemById(String id) { return foundRepo.findById(id); }
    public Optional<LostItem>  getLostItemById(String id)  { return lostRepo.findById(id); }

    public List<FoundItem> searchFoundItems(String keyword) {
        return foundRepo.searchByKeyword(keyword);
    }

    public List<LostItem> searchLostItems(String keyword) {
        return lostRepo.searchByKeyword(keyword);
    }

    public void updateFoundItemStatus(String itemId, Status status) {
        foundRepo.findById(itemId).ifPresent(item -> {
            item.setStatus(status);
            foundRepo.update(item);
        });
    }

    public SystemStats getStats() {
        return new SystemStats(
            foundRepo.count(),
            lostRepo.count(),
            claimRepo.count(),
            foundRepo.findByStatus(Status.FOUND).size(),
            lostRepo.findByStatus(Status.LOST).size(),
            foundRepo.findByStatus(Status.MATCHED).size()
        );
    }

    // ── Inner class for stats ────────────────────────────────────────────────

    public static class SystemStats {
        public final int totalFound;
        public final int totalLost;
        public final int totalClaimed;
        public final int awaitingClaim;
        public final int activeLostReports;
        public final int matched;

        public SystemStats(int totalFound, int totalLost, int totalClaimed,
                           int awaitingClaim, int activeLostReports, int matched) {
            this.totalFound = totalFound;
            this.totalLost = totalLost;
            this.totalClaimed = totalClaimed;
            this.awaitingClaim = awaitingClaim;
            this.activeLostReports = activeLostReports;
            this.matched = matched;
        }

        public void print() {
            System.out.println("\n┌─────────────────────────────────┐");
            System.out.println("│       SYSTEM STATISTICS         │");
            System.out.println("├─────────────────────────────────┤");
            System.out.printf("│  Total Found Items  : %-9d │%n", totalFound);
            System.out.printf("│  Total Lost Reports : %-9d │%n", totalLost);
            System.out.printf("│  Total Claims       : %-9d │%n", totalClaimed);
            System.out.printf("│  Awaiting Claim     : %-9d │%n", awaitingClaim);
            System.out.printf("│  Active Lost Reports: %-9d │%n", activeLostReports);
            System.out.printf("│  Matched Items      : %-9d │%n", matched);
            System.out.println("└─────────────────────────────────┘");
        }
    }
}
