package com.boulangerie.shared.utils;

import com.boulangerie.shared.dto.PageResponse;
import org.springframework.data.domain.Page;

public final class PageUtils {

    private PageUtils() {}

    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}