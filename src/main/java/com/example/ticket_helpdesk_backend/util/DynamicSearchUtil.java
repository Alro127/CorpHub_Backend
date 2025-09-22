package com.example.ticket_helpdesk_backend.util;

import com.example.ticket_helpdesk_backend.dto.PaginationDTO;
import com.example.ticket_helpdesk_backend.dto.SortDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DynamicSearchUtil {

    @PersistenceContext
    private EntityManager em;

    /**
     * Generic dynamic search
     * @param entityClass Entity class (Ticket.class, User.class, ...)
     * @param filters map filter key-value
     * @param sort field + order
     * @param pagination page + size
     * @param allowedFilters whitelist các field được phép filter
     * @param allowedSorts whitelist các field được phép sort
     * @param <T> type entity
     * @return list of entities
     */
    public <T> List<T> search(Class<T> entityClass,
                              Map<String, Object> filters,
                              SortDTO sort,
                              PaginationDTO pagination,
                              Set<String> allowedFilters,
                              Set<String> allowedSorts) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);

        // Build dynamic predicates
        List<Predicate> predicates = new ArrayList<>();
        if (filters != null) {
            filters.forEach((key, value) -> {
                if (allowedFilters.contains(key) && value != null) {
                    predicates.add(cb.equal(root.get(key), value));
                }
            });
        }
        cq.where(predicates.toArray(new Predicate[0]));

        // Apply sort
        if (sort != null && allowedSorts.contains(sort.getField())) {
            if ("desc".equalsIgnoreCase(sort.getOrder())) {
                cq.orderBy(cb.desc(root.get(sort.getField())));
            } else {
                cq.orderBy(cb.asc(root.get(sort.getField())));
            }
        }

        TypedQuery<T> query = em.createQuery(cq);

        // Apply pagination
        int page = pagination != null ? Math.max(pagination.getPage(), 1) : 1;
        int size = pagination != null ? Math.max(pagination.getSize(), 1) : 20;
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);

        return query.getResultList();
    }
}

