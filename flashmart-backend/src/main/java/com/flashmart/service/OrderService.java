package com.flashmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.flashmart.common.BizException;
import com.flashmart.entity.Coupon;
import com.flashmart.entity.FlashSaleEvent;
import com.flashmart.entity.OrderEntity;
import com.flashmart.mapper.CouponMapper;
import com.flashmart.mapper.FlashSaleEventMapper;
import com.flashmart.mapper.OrderMapper;
import com.flashmart.cache.DelayedDoubleDeleteEvictor;
import com.flashmart.redis.AtomicStockRedisService;
import com.flashmart.redis.OrderStreamPublisher;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final FlashSaleEventMapper flashSaleEventMapper;
    private final CouponMapper couponMapper;
    private final OrderMapper orderMapper;
    private final AtomicStockRedisService atomicStockRedisService;
    private final OrderStreamPublisher orderStreamPublisher;
    private final RedissonClient redissonClient;
    private final FlashSaleService flashSaleService;
    private final DelayedDoubleDeleteEvictor delayedDoubleDeleteEvictor;

    @Transactional(rollbackFor = Exception.class)
    public OrderEntity createFromFlashSale(Long userId, Long flashSaleEventId, BigDecimal amount) {
        RLock lock = redissonClient.getLock("flashmart:lock:flash-order:" + flashSaleEventId);
        boolean locked;
        try {
            locked = lock.tryLock(3, 25, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("Interrupted while acquiring lock");
        }
        if (!locked) {
            throw new BizException("System busy, try again");
        }
        try {
            FlashSaleEvent event = flashSaleEventMapper.selectById(flashSaleEventId);
            if (event == null) {
                throw new BizException("Flash sale event not found");
            }
            if (event.getStatus() == null || event.getStatus() != 1) {
                throw new BizException("Event not active");
            }
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(event.getBeginTime()) || now.isAfter(event.getEndTime())) {
                throw new BizException("Event not in time window");
            }
            Coupon coupon = couponMapper.selectById(event.getCouponId());
            if (coupon == null) {
                throw new BizException("Coupon for event not found");
            }

            atomicStockRedisService.setFlashStockIfAbsent(flashSaleEventId, event.getStock() == null ? 0 : event.getStock());
            long lua = atomicStockRedisService.tryDecrementFlash(flashSaleEventId);
            if (lua == 0) {
                throw new BizException("Flash sale sold out");
            }
            if (lua < 0) {
                atomicStockRedisService.overwriteFlashStock(flashSaleEventId, event.getStock() == null ? 0 : event.getStock());
                lua = atomicStockRedisService.tryDecrementFlash(flashSaleEventId);
                if (lua != 1) {
                    throw new BizException("Flash sale sold out");
                }
            }

            delayedDoubleDeleteEvictor.firstDelete(flashSaleService::evictActiveListCache);

            int rows = flashSaleEventMapper.update(
                    null,
                    new LambdaUpdateWrapper<FlashSaleEvent>()
                            .eq(FlashSaleEvent::getId, flashSaleEventId)
                            .gt(FlashSaleEvent::getStock, 0)
                            .setSql("stock = stock - 1"));
            if (rows == 0) {
                atomicStockRedisService.incrementFlash(flashSaleEventId);
                throw new BizException("Flash sale sold out");
            }

            OrderEntity order = new OrderEntity();
            order.setUserId(userId);
            order.setMerchantId(coupon.getMerchantId());
            order.setProductId(event.getProductId());
            order.setFlashSaleEventId(flashSaleEventId);
            order.setAmount(amount != null ? amount : BigDecimal.ONE);
            order.setStatus(1);
            try {
                orderMapper.insert(order);
            } catch (DuplicateKeyException e) {
                atomicStockRedisService.incrementFlash(flashSaleEventId);
                flashSaleEventMapper.update(
                        null,
                        new LambdaUpdateWrapper<FlashSaleEvent>()
                                .eq(FlashSaleEvent::getId, flashSaleEventId)
                                .setSql("stock = stock + 1"));
                throw new BizException("Already ordered for this event");
            }

            FlashSaleEvent fresh = flashSaleEventMapper.selectById(flashSaleEventId);
            if (fresh != null && fresh.getStock() != null) {
                atomicStockRedisService.overwriteFlashStock(flashSaleEventId, fresh.getStock());
            }
            delayedDoubleDeleteEvictor.scheduleSecondDelete(flashSaleService::evictActiveListCache);
            orderStreamPublisher.publishOrderPlaced(order.getId(), userId, flashSaleEventId, coupon.getMerchantId());
            return orderMapper.selectById(order.getId());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public java.util.List<OrderEntity> listMine(Long userId) {
        return orderMapper.selectList(
                new LambdaQueryWrapper<OrderEntity>()
                        .eq(OrderEntity::getUserId, userId)
                        .orderByDesc(OrderEntity::getId));
    }
}
