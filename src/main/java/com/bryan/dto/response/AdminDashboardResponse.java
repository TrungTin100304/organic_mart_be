package com.bryan.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record AdminDashboardResponse(
        BigDecimal todayRevenue,
        long todayOrders,
        long processingOrders,
        long deliveredOrders,
        long lowStockProducts,
        long newUsers,
        BigDecimal averageOrderValue,
        Map<String, Long> orderStatusCounts,
        List<RevenuePoint> revenue,
        List<TopProduct> topProducts,
        List<CategoryRevenue> categoryRevenue,
        List<OrderListResponse> recentOrders
) {
    public record RevenuePoint(LocalDate date, BigDecimal revenue) {}
    public record TopProduct(String name, BigDecimal sold) {}
    public record CategoryRevenue(String name, BigDecimal revenue) {}
}
