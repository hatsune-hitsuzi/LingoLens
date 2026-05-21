package com.hatsune.lingolens.module.admin.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.module.user.domain.UserDO;
import com.hatsune.lingolens.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Component
@RequiredArgsConstructor
public class AdminUserManager {

    private final UserMapper userMapper;

    public UserDO queryByUserId(String userId) {
        UserDO user = userMapper.selectOne(UserDO.builder().userId(userId).build());
        if (user == null) {
            throw new BizException("user not found");
        }
        return user;
    }
}
