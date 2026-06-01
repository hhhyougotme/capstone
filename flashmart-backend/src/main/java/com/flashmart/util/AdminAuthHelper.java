package com.flashmart.util;

import com.flashmart.common.BizException;
import com.flashmart.entity.User;
import com.flashmart.mapper.UserMapper;
import com.flashmart.service.SessionService;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AdminAuthHelper {

    public static Long requireAdmin(SessionService sessionService, UserMapper userMapper, String authorizationHeader) {
        Long userId = AuthHelper.requireUserId(sessionService, authorizationHeader);
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() == null || user.getRole() != User.ROLE_ADMIN) {
            throw new BizException(403, "Admin access required");
        }
        return userId;
    }
}
