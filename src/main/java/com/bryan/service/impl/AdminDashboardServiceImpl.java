package com.bryan.service.impl;

import com.bryan.dto.response.AdminDashboardResponse;
import com.bryan.dto.response.OrderListResponse;
import com.bryan.entity.Order;
import com.bryan.entity.OrderStatus;
import com.bryan.repository.InventoryBatchRepository;
import com.bryan.repository.OrderDetailRepository;
import com.bryan.repository.OrderRepository;
import com.bryan.repository.UserRepository;
import com.bryan.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final Set<OrderStatus> REVENUE_EXCLUDED =
            Set.of(OrderStatus.CANCELLED, OrderStatus.REFUNDED);
    private static final Set<OrderStatus> PROCESSING =
            Set.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
                    OrderStatus.READY_FOR_DELIVERY, OrderStatus.DELIVERING);

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final UserRepository userRepository;

    @Override
    public AdminDashboardResponse getDashboard(int requestedDays) {
        int days = Math.max(1, Math.min(requestedDays, 90));
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rangeStart = today.minusDays(days - 1L).atStartOfDay();
        List<Order> orders = orderRepository.findByCreatedAtBetween(rangeStart, now);

        List<Order> revenueOrders = orders.stream()
                .filter(order -> !REVENUE_EXCLUDED.contains(order.getStatus()))
                .toList();
        List<Order> todayOrders = orders.stream()
                .filter(order -> order.getCreatedAt() != null
                        && order.getCreatedAt().toLocalDate().equals(today))
                .toList();

        BigDecimal todayRevenue = todayOrders.stream()
                .filter(order -> !REVENUE_EXCLUDED.contains(order.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRevenue = revenueOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageOrderValue = revenueOrders.isEmpty()
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(revenueOrders.size()), 2, RoundingMode.HALF_UP);

        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status, orderRepository.countByStatus(status));
        }
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        counts.forEach((key, value) -> statusCounts.put(key.name(), value));

        Map<LocalDate, BigDecimal> revenueByDay = new LinkedHashMap<>();
        for (int offset = days - 1; offset >= 0; offset--) {
            revenueByDay.put(today.minusDays(offset), BigDecimal.ZERO);
        }
        revenueOrders.forEach(order -> {
            if (order.getCreatedAt() != null) {
                LocalDate date = order.getCreatedAt().toLocalDate();
                revenueByDay.computeIfPresent(date, (ignored, value) -> value.add(order.getTotalAmount()));
            }
        });

        List<AdminDashboardResponse.RevenuePoint> revenue = revenueByDay.entrySet().stream()
                .map(entry -> new AdminDashboardResponse.RevenuePoint(entry.getKey(), entry.getValue()))
                .toList();
        List<AdminDashboardResponse.TopProduct> topProducts = orderDetailRepository
                .findTopProducts(REVENUE_EXCLUDED, PageRequest.of(0, 5))
                .stream()
                .map(row -> new AdminDashboardResponse.TopProduct((String) row[0], (BigDecimal) row[1]))
                .toList();
        List<AdminDashboardResponse.CategoryRevenue> categoryRevenue = orderDetailRepository
                .findCategoryRevenue(REVENUE_EXCLUDED)
                .stream()
                .map(row -> new AdminDashboardResponse.CategoryRevenue((String) row[0], (BigDecimal) row[1]))
                .toList();
        List<OrderListResponse> recentOrders = orderRepository
                .findAllOrders(PageRequest.of(0, 5, Sort.by("createdAt").descending()))
                .map(OrderListResponse::from)
                .getContent();

        long processingOrders = PROCESSING.stream().mapToLong(status -> counts.getOrDefault(status, 0L)).sum();
        return new AdminDashboardResponse(
                todayRevenue,
                todayOrders.size(),
                processingOrders,
                counts.getOrDefault(OrderStatus.DELIVERED, 0L),
                inventoryBatchRepository.countLowStockProducts(new BigDecimal("10")),
                userRepository.countByCreatedAtAfter(now.minusDays(7)),
                averageOrderValue,
                statusCounts,
                revenue,
                topProducts,
                categoryRevenue,
                recentOrders
        );
    }
}
