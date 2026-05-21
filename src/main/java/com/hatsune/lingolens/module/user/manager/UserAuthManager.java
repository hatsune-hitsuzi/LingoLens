package com.hatsune.lingolens.module.user.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import com.hatsune.lingolens.infrastructure.id.DistributedIdGenerator;
import com.hatsune.lingolens.infrastructure.jwt.JwtTokenProvider;
import com.hatsune.lingolens.module.user.domain.UserDO;
import com.hatsune.lingolens.module.user.dto.RegisterRequest;
import com.hatsune.lingolens.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Component
@RequiredArgsConstructor
public class UserAuthManager {

    private static final Duration TOKEN_TTL = Duration.ofDays(7);
    private static final String USER_ID_BIZ_TYPE = "user";
    private static final int USER_ID_LENGTH = 8;

    private final UserMapper userMapper;
    private final DistributedIdGenerator distributedIdGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${lingolens.user.user-id-prefix:10}")
    private String userIdPrefix;

    public UserDO registerUser(RegisterRequest request) {
        Long id = distributedIdGenerator.id(USER_ID_BIZ_TYPE);
        UserDO user = UserDO.builder()
                .id(id)
                .userId(nextUserIdValue())
                .nickname(request.getNickname())
                .avatarUrl(request.getAvatarUrl())
                .passwordHash(encodePassword(request.getPassword()))
                .build();
        userMapper.insert(user);
        return user;
    }
    public boolean nicknameExists(String nickname) {
        return userMapper.selectOne(UserDO.builder().nickname(nickname).build()) != null;
    }
    public Optional<UserDO> findByAccount(String account) {
        UserDO user = userMapper.selectOne(UserDO.builder().userId(account).build());
        if (user == null) {
            user = userMapper.selectOne(UserDO.builder().nickname(account).build());
        }
        return Optional.ofNullable(user);
    }

    public Optional<UserDO> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMapper.selectByPrimaryKey(UserDO.builder().id(id).build()));
    }

    public boolean passwordMatches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public String nextUserIdValue() {
        if (!StringUtils.hasText(userIdPrefix) || !userIdPrefix.matches("\\d+")) {
            throw new BizException("user_id prefix must be numeric");
        }
        int sequenceWidth = USER_ID_LENGTH - userIdPrefix.length();
        if (sequenceWidth <= 0) {
            throw new BizException("user_id prefix is too long");
        }
        Long sequence = userMapper.nextUserIdSequence();
        if (sequence == null) {
            throw new BizException("failed to generate user_id");
        }
        long maxSequence = (long) Math.pow(10, sequenceWidth) - 1;
        if (sequence > maxSequence) {
            throw new BizException("user_id sequence is exhausted");
        }
        return userIdPrefix + String.format("%0" + sequenceWidth + "d", sequence);
    }

    public String issueToken(Long userId) {
        String token = jwtTokenProvider.createToken(userId, TOKEN_TTL);
        stringRedisTemplate.opsForValue().set(RedisKeyConstant.AUTH_TOKEN.key(token), String.valueOf(userId), TOKEN_TTL);
        return token;
    }

    public Duration tokenTtl() {
        return TOKEN_TTL;
    }
}
