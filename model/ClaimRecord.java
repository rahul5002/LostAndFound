package com.lostandfound.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records a successful claim transaction when an owner retrieves their item.
 */
public class ClaimRecord {
    private final String claimId;
    private final FoundItem foundItem;
    private final Person claimant;
    private final LocalDateTime claimTime;
    private final String verificationNotes;
    private String handledBy;     // Staff member who processed the claim

    public ClaimRecord(FoundItem foundItem, Person claimant, String verificationNotes, String handledBy) {
        this.claimId = "CLM-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.foundItem = foundItem;
        this.claimant = claimant;
        this.claimTime = LocalDateTime.now();
        this.verificationNotes = verificationNotes;
        this.handledBy = handledBy;
    }

    public String getClaimId() { return claimId; }
    public FoundItem getFoundItem() { return foundItem; }
    public Person getClaimant() { return claimant; }
    public LocalDateTime getClaimTime() { return claimTime; }
    public String getVerificationNotes() { return verificationNotes; }
    public String getHandledBy() { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }

    public String getReceipt() {
        return String.format(
            "\n=========================================\n" +
            "       LOST & FOUND CLAIM RECEIPT        \n" +
            "=========================================\n" +
            " Claim ID   : %s\n" +
            " Item ID    : %s\n" +
            " Item Name  : %s\n" +
            " Claimed By : %s\n" +
            " Contact    : %s\n" +
            " Date/Time  : %s\n" +
            " Handled By : %s\n" +
            " Notes      : %s\n" +
            "=========================================\n" +
            "     Thank you! Item successfully claimed.\n" +
            "=========================================\n",
            claimId,
            foundItem.getItemId(),
            foundItem.getName(),
            claimant.getName(),
            claimant.getContactNumber(),
            claimTime.toString().replace("T", " ").substring(0, 16),
            (handledBy != null ? handledBy : "System"),
            (verificationNotes != null ? verificationNotes : "None")
        );
    }

    @Override
    public String toString() {
        return String.format("ClaimRecord[%s] Item:%s by %s on %s",
                claimId, foundItem.getItemId(), claimant.getName(), claimTime);
    }
}
