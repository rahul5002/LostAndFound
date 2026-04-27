package com.lostandfound.repository;

import com.lostandfound.model.ClaimRecord;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for ClaimRecord objects.
 */
public class ClaimRecordRepository implements Repository<ClaimRecord, String> {

    private final Map<String, ClaimRecord> store = new LinkedHashMap<>();

    @Override
    public void save(ClaimRecord record) {
        store.put(record.getClaimId(), record);
    }

    @Override
    public Optional<ClaimRecord> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ClaimRecord> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void update(ClaimRecord record) {
        store.put(record.getClaimId(), record);
    }

    @Override
    public boolean delete(String id) {
        return store.remove(id) != null;
    }

    @Override
    public int count() {
        return store.size();
    }

    public List<ClaimRecord> findByClaimantId(String personId) {
        return store.values().stream()
                .filter(r -> r.getClaimant().getPersonId().equals(personId))
                .collect(Collectors.toList());
    }

    public Optional<ClaimRecord> findByFoundItemId(String foundItemId) {
        return store.values().stream()
                .filter(r -> r.getFoundItem().getItemId().equals(foundItemId))
                .findFirst();
    }
}
