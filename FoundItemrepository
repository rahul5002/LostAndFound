package com.lostandfound.repository;

import com.lostandfound.enums.Category;
import com.lostandfound.enums.Status;
import com.lostandfound.model.FoundItem;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for FoundItem objects.
 * Simulates a database with HashMap-backed storage.
 */
public class FoundItemRepository implements Repository<FoundItem, String> {

    private final Map<String, FoundItem> store = new LinkedHashMap<>();

    @Override
    public void save(FoundItem item) {
        if (item == null) throw new IllegalArgumentException("Cannot save null FoundItem");
        store.put(item.getItemId(), item);
    }

    @Override
    public Optional<FoundItem> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<FoundItem> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void update(FoundItem item) {
        if (!store.containsKey(item.getItemId())) {
            throw new NoSuchElementException("FoundItem not found: " + item.getItemId());
        }
        store.put(item.getItemId(), item);
    }

    @Override
    public boolean delete(String id) {
        return store.remove(id) != null;
    }

    @Override
    public int count() {
        return store.size();
    }

    // ── Domain-specific queries ──────────────────────────────────────────────

    public List<FoundItem> findByStatus(Status status) {
        return store.values().stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<FoundItem> findByCategory(Category category) {
        return store.values().stream()
                .filter(i -> i.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<FoundItem> findByStatusAndCategory(Status status, Category category) {
        return store.values().stream()
                .filter(i -> i.getStatus() == status && i.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<FoundItem> findActiveItems() {
        return store.values().stream()
                .filter(i -> i.getStatus() == Status.FOUND || i.getStatus() == Status.MATCHED)
                .filter(i -> !i.isExpired())
                .collect(Collectors.toList());
    }

    public List<FoundItem> findFoundBetween(LocalDateTime from, LocalDateTime to) {
        return store.values().stream()
                .filter(i -> {
                    LocalDateTime ft = i.getFoundTime();
                    return ft != null && !ft.isBefore(from) && !ft.isAfter(to);
                })
                .collect(Collectors.toList());
    }

    public List<FoundItem> searchByKeyword(String keyword) {
        String kw = keyword.toLowerCase();
        return store.values().stream()
                .filter(i -> matches(i, kw))
                .collect(Collectors.toList());
    }

    private boolean matches(FoundItem item, String kw) {
        return (item.getName() != null && item.getName().toLowerCase().contains(kw)) ||
               (item.getDescription() != null && item.getDescription().toLowerCase().contains(kw)) ||
               (item.getColor() != null && item.getColor().toLowerCase().contains(kw)) ||
               (item.getBrand() != null && item.getBrand().toLowerCase().contains(kw));
    }
}
