package com.flashmart.controller;

import com.flashmart.common.ApiResult;
import com.flashmart.dto.PlaceOrderRequest;
import com.flashmart.entity.OrderEntity;
import com.flashmart.service.OrderService;
import com.flashmart.service.SessionService;
import com.flashmart.util.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SessionService sessionService;

    @PostMapping("/flash-sales/{eventId}/orders")
    public ApiResult<OrderEntity> place(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long eventId,
            @RequestBody(required = false) PlaceOrderRequest body) {
        Long userId = AuthHelper.requireUserId(sessionService, authorization);
        var amount = body != null ? body.getAmount() : null;
        return ApiResult.ok(orderService.createFromFlashSale(userId, eventId, amount));
    }

    @GetMapping("/me/orders")
    public ApiResult<List<OrderEntity>> mine(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = AuthHelper.requireUserId(sessionService, authorization);
        return ApiResult.ok(orderService.listMine(userId));
    }
}
