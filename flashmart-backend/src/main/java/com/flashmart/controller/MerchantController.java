package com.flashmart.controller;

import com.flashmart.common.ApiResult;
import com.flashmart.dto.PageResult;
import com.flashmart.entity.Merchant;
import com.flashmart.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    public ApiResult<PageResult<Merchant>> list(
            @RequestParam(required = false) Long merchantTypeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(merchantService.listPage(merchantTypeId, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Merchant> get(@PathVariable Long id) {
        return ApiResult.ok(merchantService.getById(id));
    }
}
