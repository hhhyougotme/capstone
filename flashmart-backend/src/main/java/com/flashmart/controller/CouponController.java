package com.flashmart.controller;

import com.flashmart.common.ApiResult;
import com.flashmart.entity.Coupon;
import com.flashmart.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ApiResult<List<Coupon>> list(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        if (activeOnly) {
            return ApiResult.ok(couponService.listActiveByMerchant(merchantId));
        }
        return ApiResult.ok(couponService.listByMerchant(merchantId));
    }
}
