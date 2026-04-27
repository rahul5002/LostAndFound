package com.lostandfound.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface defining standard CRUD operations.
 * @param <T>  The entity type
 * @param <ID> The ID type
 */
public interface Repository<T, ID> {
    void save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void update(T entity);
    boolean delete(ID id);
    int count();
}
