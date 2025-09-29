package com.avaneesh.yodha.Eventify.repository.specifications;

import com.avaneesh.yodha.Eventify.entities.Events;
import com.avaneesh.yodha.Eventify.entities.Seat;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecification {

    public static Specification<Events> getEvents(
            String name, String category, LocalDateTime startDate, LocalDateTime endDate, Double minPrice, Double maxPrice) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (category != null && !category.isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("category")), category.toLowerCase()));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventTimestamp"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventTimestamp"), endDate));
            }
            if (minPrice != null || maxPrice != null) {
                Join<Events, Seat> seatJoin = root.join("seats");

                if (minPrice != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(seatJoin.get("seatPricing"), minPrice));
                }
                if (maxPrice != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(seatJoin.get("seatPricing"), maxPrice));
                }
            }
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}