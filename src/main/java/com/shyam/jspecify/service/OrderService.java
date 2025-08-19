package com.shyam.jspecify.service;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
@NullMarked
public class OrderService {
    public @Nullable String getOrder(@NonNull String id, @Nullable Integer page) {
        if(page > 10) {
            return "Invalid Order Id";
        }
        return StringUtils.isNumeric(id) ? null : "Order details for id: " + id;
    }
}
