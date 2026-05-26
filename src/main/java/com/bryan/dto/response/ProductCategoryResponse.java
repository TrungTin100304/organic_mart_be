package com.bryan.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProductCategoryResponse(
    Long id,
    String name,
    String slug,
    Long parentId,
    int sortOrder,
    LocalDateTime createdAt
) {}

