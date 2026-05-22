package com.hatsune.lingolens.module.user.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import com.hatsune.lingolens.module.user.domain.UserDO;
import com.hatsune.lingolens.module.user.dto.UserPasswordUpdateRequest;
import com.hatsune.lingolens.module.user.dto.UserProfileUpdateRequest;
import com.hatsune.lingolens.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Component
@RequiredArgsConstructor
public class UserAccountManager {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    public UserDO getProfile(Long id) {
        UserDO user = userMapper.selectByPrimaryKey(UserDO.builder().id(id).build());
        if (user == null) {
            throw new BizException("user not found");
        }
        return user;
    }

    public UserDO updateProfile(Long id, UserProfileUpdateRequest request) {
        UserDO currentUser = getProfile(id);
        if (StringUtils.hasText(request.getNickname())) {
            UserDO nicknameUser = userMapper.selectOne(UserDO.builder().nickname(request.getNickname()).build());
            if (nicknameUser != null && !id.equals(nicknameUser.getId())) {
                throw new BizException("nickname already exists");
            }
        }
        if (StringUtils.hasText(request.getEmail())) {
            UserDO emailUser = userMapper.selectOne(UserDO.builder().email(request.getEmail()).build());
            if (emailUser != null && !id.equals(emailUser.getId())) {
                throw new BizException("email already exists");
            }
        }

        UserDO updateUser = UserDO.builder()
                .id(id)
                .nickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : null)
                .avatarUrl(StringUtils.hasText(request.getAvatarUrl()) ? request.getAvatarUrl() : null)
                .email(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null)
                .bio(request.getBio() != null ? request.getBio() : null)
                .updateTime(OffsetDateTime.now())
                .build();
        userMapper.updateByPrimaryKey(updateUser);
        return UserDO.builder()
                .id(currentUser.getId())
                .userId(currentUser.getUserId())
                .nickname(StringUtils.hasText(updateUser.getNickname()) ? updateUser.getNickname() : currentUser.getNickname())
                .avatarUrl(StringUtils.hasText(updateUser.getAvatarUrl()) ? updateUser.getAvatarUrl() : currentUser.getAvatarUrl())
                .email(StringUtils.hasText(updateUser.getEmail()) ? updateUser.getEmail() : currentUser.getEmail())
                .bio(updateUser.getBio() != null ? updateUser.getBio() : currentUser.getBio())
                .build();
    }

    public void updatePassword(Long id, UserPasswordUpdateRequest request) {
        UserDO user = getProfile(id);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BizException("old password is invalid");
        }
        UserDO updateUser = UserDO.builder()
                .id(id)
                .passwordHash(passwordEncoder.encode(request.getNewPassword()))
                .updateTime(OffsetDateTime.now())
                .build();
        userMapper.updateByPrimaryKey(updateUser);
    }

    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            stringRedisTemplate.delete(RedisKeyConstant.AUTH_TOKEN.key(token));
        }
    }
}
