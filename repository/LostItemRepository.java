package com.lostandfound.repository;

import com.lostandfound.enums.Category;
import com.lostandfound.enums.Status;
import com.lostandfound.model.LostItem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for LostItem objects.
 */
public class LostItemRepository implements Repository<LostItem, String> {

    private final Map<String, LostItem> store = new LinkedHashMap<>();

    @Override
    public void save(LostItem item) {
        if (item == null) throw new IllegalArgumentException("Cannot save null LostItem");
        store.put(item.getItemId(), item);
    }

    @Override
    public Optional<LostItem> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<LostItem> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void update(LostItem item) {
        if (!store.containsKey(item.getItemId())) {
            throw new NoSuchElementException("LostItem not found: " + item.getItemId());
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

    public List<LostItem> findByStatus(Status status) {
        return store.values().stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<LostItem> findByCategory(Category category) {
        return store.values().stream()
                .filter(i -> i.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<LostItem> findUnmatched() {
        return store.values().stream()
                .filter(i -> i.getStatus() == Status.LOST && i.getMatchedFoundItemId() == null)
                .collect(Collectors.toList());
    }

    public List<LostItem> searchByKeyword(String keyword) {
        String kw = keyword.toLowerCase();
        return store.values().stream()
                .filter(i ->
                    (i.getName() != null && i.getName().toLowerCase().contains(kw)) ||
                    (i.getDescription() != null && i.getDescription().toLowerCase().contains(kw)) ||
                    (i.getColor() != null && i.getColor().toLowerCase().contains(kw)) ||
                    (i.getBrand() != null && i.getBrand().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
    }
}
