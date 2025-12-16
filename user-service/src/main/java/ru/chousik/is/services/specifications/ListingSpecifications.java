package ru.chousik.is.services.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.chousik.is.dto.listing.ListingSearchRequest;
import ru.chousik.is.entity.AvailabilitySlot;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.ListingCategory;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class ListingSpecifications {

    private ListingSpecifications() {
    }

    public static Specification<Listing> fromRequest(ListingSearchRequest request) {
        Specification<Listing> spec = Specification.where(null);
        if (request == null) {
            return spec;
        }
        spec = spec.and(textContains(request.text()));
        spec = spec.and(categoryEquals(request.categoryId()));
        spec = spec.and(priceBetween(request.minPrice(), request.maxPrice()));
        spec = spec.and(availableBetween(request.availableFrom(), request.availableTo()));
        spec = spec.and(latitudeBetween(request.minLatitude(), request.maxLatitude()));
        spec = spec.and(longitudeBetween(request.minLongitude(), request.maxLongitude()));
        return spec;
    }

    private static Specification<Listing> textContains(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String pattern = "%" + text.toLowerCase() + "%";
        return (root, query, cb) -> {
            var basePredicate = cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
            var subquery = query.subquery(UUID.class);
            var listingCategory = subquery.from(ListingCategory.class);
            subquery.select(listingCategory.get("listing").get("id"));
            subquery.where(
                    cb.equal(listingCategory.get("listing").get("id"), root.get("id")),
                    cb.like(cb.lower(listingCategory.get("category").get("name")), pattern)
            );
            return cb.or(basePredicate, cb.exists(subquery));
        };
    }

    private static Specification<Listing> categoryEquals(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, cb) -> {
            var subquery = query.subquery(UUID.class);
            var listingCategory = subquery.from(ListingCategory.class);
            subquery.select(listingCategory.get("listing").get("id"));
            subquery.where(
                    cb.equal(listingCategory.get("listing").get("id"), root.get("id")),
                    cb.equal(listingCategory.get("category").get("id"), categoryId)
            );
            return cb.exists(subquery);
        };
    }

    private static Specification<Listing> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (minPrice != null) {
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(root.get("pricePerHour"), minPrice));
            }
            if (maxPrice != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(root.get("pricePerHour"), maxPrice));
            }
            return predicates;
        };
    }

    private static Specification<Listing> availableBetween(OffsetDateTime from, OffsetDateTime to) {
        if (from == null && to == null) {
            return null;
        }
        return (root, query, cb) -> {
            var subquery = query.subquery(UUID.class);
            var slot = subquery.from(AvailabilitySlot.class);
            subquery.select(slot.get("listing").get("id"));

            var predicates = cb.conjunction();
            predicates.getExpressions().add(cb.equal(slot.get("listing").get("id"), root.get("id")));
            if (from != null && to != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(slot.get("startsAt"), from));
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(slot.get("endsAt"), to));
            } else if (from != null) {
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(slot.get("endsAt"), from));
            } else if (to != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(slot.get("startsAt"), to));
            }
            subquery.where(predicates);
            return cb.exists(subquery);
        };
    }

    private static Specification<Listing> latitudeBetween(BigDecimal minLatitude, BigDecimal maxLatitude) {
        if (minLatitude == null && maxLatitude == null) {
            return null;
        }
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (minLatitude != null) {
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(root.get("latitude"), minLatitude));
            }
            if (maxLatitude != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(root.get("latitude"), maxLatitude));
            }
            return predicates;
        };
    }

    private static Specification<Listing> longitudeBetween(BigDecimal minLongitude, BigDecimal maxLongitude) {
        if (minLongitude == null && maxLongitude == null) {
            return null;
        }
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (minLongitude != null) {
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(root.get("longitude"), minLongitude));
            }
            if (maxLongitude != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(root.get("longitude"), maxLongitude));
            }
            return predicates;
        };
    }
}
