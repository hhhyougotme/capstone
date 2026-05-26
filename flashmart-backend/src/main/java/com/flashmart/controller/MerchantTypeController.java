package com.flashmart.controller;

import com.flashmart.common.ApiResult;
import com.flashmart.entity.MerchantType;
import com.flashmart.service.MerchantTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/merchant-types")
@RequiredArgsConstructor
public class MerchantTypeController {

    private final MerchantTypeService merchantTypeService;

    @GetMapping
    public ApiResult<List<MerchantType>> list() {
        return ApiResult.ok(merchantTypeService.listAll());
    }
}
