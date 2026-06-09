package com.bryan.repository.specification;

import com.bryan.entity.DeliveryMethod;
import com.bryan.entity.Order;
import com.bryan.entity.OrderStatus;
import com.bryan.entity.PaymentRequest;
import com.bryan.entity.PaymentMethod;
import com.bryan.entity.PaymentStatus;
import com.bryan.entity.Review;
import com.bryan.entity.Role;
import com.bryan.entity.SepayWebhookEvent;
import com.bryan.entity.User;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AdminSearchSpecifications {

    private AdminSearchSpecifications() {
    }

    public static Specification<User> users(String search, Role role, Boolean active) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String normalizedSearch = normalize(search);

            if (normalizedSearch != null) {
                String pattern = containsPattern(normalizedSearch);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                        criteriaBuilder.like(root.get("phoneNumber"), containsRawPattern(normalizedSearch))));
            }
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), active));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Order> orders(
            OrderStatus status,
            PaymentMethod paymentMethod,
            LocalDate fromDate,
            LocalDate toDate,
            String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String normalizedSearch = normalize(search);

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (paymentMethod != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), paymentMethod));
            }
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), toDate.plusDays(1).atStartOfDay()));
            }
            if (normalizedSearch != null) {
                String pattern = containsPattern(normalizedSearch);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("orderCode")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("recipientNameSnapshot")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("recipientPhoneSnapshot")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("email")), pattern)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Order> deliveryOrders(
            LocalDate deliveryDate,
            String buildingCode,
            Long deliverySlotId,
            DeliveryMethod deliveryMethod,
            OrderStatus status,
            String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String normalizedBuildingCode = normalize(buildingCode);
            String normalizedSearch = normalize(search);

            if (deliveryDate != null) {
                predicates.add(criteriaBuilder.equal(root.get("deliveryDate"), deliveryDate));
            }
            if (normalizedBuildingCode != null) {
                predicates.add(criteriaBuilder.equal(root.get("buildingCodeSnapshot"), normalizedBuildingCode));
            }
            if (deliverySlotId != null) {
                predicates.add(criteriaBuilder.equal(root.get("deliverySlotId"), deliverySlotId));
            }
            if (deliveryMethod != null) {
                predicates.add(criteriaBuilder.equal(root.get("deliveryMethod"), deliveryMethod));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (normalizedSearch != null) {
                String pattern = containsPattern(normalizedSearch);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("orderCode")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("recipientNameSnapshot")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("recipientPhoneSnapshot")), pattern)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<PaymentRequest> paymentRequests(PaymentStatus status, String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String normalizedSearch = normalize(search);

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (normalizedSearch != null) {
                String pattern = containsPattern(normalizedSearch);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("transferCode")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("transactionId")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(
                                root.join("order", JoinType.LEFT).get("orderCode")), pattern)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<SepayWebhookEvent> webhookEvents(
            SepayWebhookEvent.EventStatus status,
            String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String normalizedSearch = normalize(search);

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (normalizedSearch != null) {
                String pattern = containsPattern(normalizedSearch);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sepayTransactionId")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("referenceCode")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("transferCode")), pattern)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Review> reviews(Review.ReviewStatus status, String productName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String normalizedProductName = normalize(productName);

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (normalizedProductName != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("product").get("name")),
                        containsPattern(normalizedProductName)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String containsPattern(String value) {
        return "%" + value.toLowerCase(Locale.ROOT) + "%";
    }

    private static String containsRawPattern(String value) {
        return "%" + value + "%";
    }
}
