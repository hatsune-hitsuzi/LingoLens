package com.hatsune.lingolens.common.auth;

import com.hatsune.lingolens.common.exception.BizException;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        Long userId = USER_ID_HOLDER.get();
        if (userId == null) {
            throw new BizException("login expired, please login again");
        }
        return userId;
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
