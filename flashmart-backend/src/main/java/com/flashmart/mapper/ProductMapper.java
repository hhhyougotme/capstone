package com.flashmart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flashmart.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
