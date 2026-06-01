package com.flashmart.controller;

import com.flashmart.common.ApiResult;
import com.flashmart.dto.PlaceOrderRequest;
import com.flashmart.dto.PlaceOrderResponse;
import com.flashmart.dto.PlaceProductOrderRequest;
import jakarta.validation.Valid;
import com.flashmart.entity.OrderEntity;
import com.flashmart.service.OrderResultService;
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
    private final OrderResultService orderResultService;

    @PostMapping("/products/{productId}/orders")
    public ApiResult<PlaceOrderResponse> placeProduct(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long productId,
            @Valid @RequestBody(required = false) PlaceProductOrderRequest body) {
        Long userId = AuthHelper.requireUserId(sessionService, authorization);
        Integer qty = body != null ? body.getQuantity() : 1;
        return ApiResult.ok(orderService.placeProductOrder(userId, productId, qty));
    }

    @PostMapping("/flash-sales/{eventId}/orders")
    public ApiResult<PlaceOrderResponse> place(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long eventId,
            @RequestBody(required = false) PlaceOrderRequest body) {
        Long userId = AuthHelper.requireUserId(sessionService, authorization);
        var amount = body != null ? body.getAmount() : null;
        return ApiResult.ok(orderService.placeFlashSaleOrder(userId, eventId, amount));
    }

    @GetMapping("/flash-sales/orders/result/{correlationId}")
    public ApiResult<PlaceOrderResponse> orderResult(@PathVariable String correlationId) {
        PlaceOrderResponse r = orderResultService.get(correlationId);
        return ApiResult.ok(r);
    }

    @GetMapping("/me/orders")
    public ApiResult<List<OrderEntity>> mine(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = AuthHelper.requireUserId(sessionService, authorization);
        return ApiResult.ok(orderService.listMine(userId));
    }
}
