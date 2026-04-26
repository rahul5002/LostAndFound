package com.lostandfound.service;

import com.lostandfound.model.ClaimRecord;
import com.lostandfound.model.FoundItem;
import com.lostandfound.model.LostItem;
import com.lostandfound.model.MatchResult;
import com.lostandfound.model.Person;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles all notification output to the console.
 * In a real system this would send emails/SMS; here it prints formatted messages.
 */
public class NotificationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String DIVIDER = "═".repeat(56);

    public void notifyMatchFound(Person person, List<MatchResult> matches) {
        System.out.println("\n" + DIVIDER);
        System.out.println("  📢  NOTIFICATION TO: " + person.getName().toUpperCase());
        System.out.println(DIVIDER);
        System.out.println("  GREAT NEWS! We found " + matches.size() +
                " possible match(es) for your lost item.\n");

        for (int i = 0; i < matches.size(); i++) {
            MatchResult mr = matches.get(i);
            System.out.printf("  [Match #%d]  Confidence: %s  (%.1f%%)%n",
                    i + 1, mr.getConfidenceLevel().getStars(), mr.getOverallScore() * 100);
            System.out.println("  FoundItem ID : " + mr.getFoundItem().getItemId());
            System.out.println("  Item Name    : " + mr.getFoundItem().getName());
            System.out.println("  Found At     : " + (mr.getFoundItem().getFoundLocation() != null ?
                    mr.getFoundItem().getFoundLocation() : "Unknown"));
            System.out.println("  Found On     : " + (mr.getFoundItem().getFoundTime() != null ?
                    mr.getFoundItem().getFoundTime().format(FMT) : "Unknown"));
            System.out.println("  Storage      : " + (mr.getFoundItem().getStorageLocation() != null ?
                    mr.getFoundItem().getStorageLocation() : "Main Office"));
            System.out.println();
        }

        System.out.println("  ➜  Please visit the Lost & Found office to verify and");
        System.out.println("     claim your item. Bring a valid ID.");
        System.out.println("  ➜  Contact: lostandfound@facility.com | Ext: 100");
        System.out.println(DIVIDER + "\n");
    }

    public void notifyNoMatchFound(Person person, LostItem lostItem) {
        System.out.println("\n" + DIVIDER);
        System.out.println("  📋  NOTIFICATION TO: " + person.getName().toUpperCase());
        System.out.println(DIVIDER);
        System.out.println("  No matching item was found in our database at this time.");
        System.out.println();
        System.out.println("  Your lost item report has been registered:");
        System.out.println("  Reference ID : " + lostItem.getItemId());
        System.out.println("  Item         : " + lostItem.getName());
        System.out.println("  Category     : " + lostItem.getCategory());
        System.out.println();
        System.out.println("  ➜  We will notify you if a matching item is found.");
        System.out.println("  ➜  Keep your reference ID for follow-up queries.");
        System.out.println("  ➜  Contact: lostandfound@facility.com | Ext: 100");
        System.out.println(DIVIDER + "\n");
    }

    public void notifyItemRegistered(FoundItem item) {
        System.out.println("\n" + DIVIDER);
        System.out.println("  ✅  FOUND ITEM REGISTERED SUCCESSFULLY");
        System.out.println(DIVIDER);
        System.out.println("  Item ID      : " + item.getItemId());
        System.out.println("  Name         : " + item.getName());
        System.out.println("  Category     : " + item.getCategory());
        System.out.println("  Storage      : " + (item.getStorageLocation() != null ?
                item.getStorageLocation() : "Main Office"));
        System.out.println("  Expires On   : " + item.getExpiryDate().format(FMT));
        System.out.println(DIVIDER + "\n");
    }

    public void notifyClaimProcessed(ClaimRecord record) {
        System.out.println(record.getReceipt());
    }

    public void notifyAutoMatchFound(LostItem lost, FoundItem found, double score) {
        System.out.println("\n  🔔  AUTO-MATCH ALERT");
        System.out.println("  Newly registered found item matches existing lost report!");
        System.out.printf("  Lost Item [%s] ↔ Found Item [%s]  Score: %.1f%%%n",
                lost.getItemId(), found.getItemId(), score * 100);
        System.out.println("  Please notify: " + lost.getReportedBy().getName() +
                " at " + lost.getReportedBy().getContactNumber());
    }
}
