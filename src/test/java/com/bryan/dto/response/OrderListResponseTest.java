package com.bryan.dto.response;

import com.bryan.entity.Order;
import com.bryan.entity.OrderDetail;
import com.bryan.entity.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderListResponseTest {

    @Test
    void countsOneProductWhenInventoryWasAllocatedAcrossMultipleBatches() {
        Product product = new Product();
        product.setId(9L);
        Order order = new Order();

        OrderDetail firstBatch = new OrderDetail();
        firstBatch.setProduct(product);
        order.addDetail(firstBatch);

        OrderDetail secondBatch = new OrderDetail();
        secondBatch.setProduct(product);
        order.addDetail(secondBatch);

        assertEquals(1, OrderListResponse.from(order).itemCount());
    }
}
