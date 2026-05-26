package com.flashmart.service;

import com.flashmart.entity.MerchantType;
import com.flashmart.mapper.MerchantTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantTypeService {

    private final MerchantTypeMapper merchantTypeMapper;

    public List<MerchantType> listAll() {
        return merchantTypeMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<MerchantType>lambdaQuery()
                        .orderByAsc(MerchantType::getSortOrder));
    }
}
