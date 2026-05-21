package com.hatsune.lingolens.module.admin.service.impl;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.admin.dto.AdminUserInfoDTO;
import com.hatsune.lingolens.module.admin.dto.AdminUserQueryByUserIdRequest;
import com.hatsune.lingolens.module.admin.dto.AdminUserQueryResponse;
import com.hatsune.lingolens.module.admin.manager.AdminUserManager;
import com.hatsune.lingolens.module.admin.service.AdminUserService;
import com.hatsune.lingolens.module.user.domain.UserDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserManager adminUserManager;
    private final CommonTools commonTools;

    @Override
    public AdminUserQueryResponse queryByUserId(@RequestBody String requestJson) {
        log.info("admin user query by user id request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            AdminUserQueryByUserIdRequest request = commonTools.parseJson(requestJson, AdminUserQueryByUserIdRequest.class);
            commonTools.validateByJsr303(request);
            request.setUserId(request.getUserId().trim());
            Assert.hasText(request.getUserId(), "user id must not be blank");
            UserDO user = adminUserManager.queryByUserId(request.getUserId());
            return buildSuccessResponse(user);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("admin user query by user id failed, reason={}", e.getMessage());
            return buildFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("admin user query by user id failed by system error", e);
            return buildFailResponse("system busy, please try again later");
        }
    }

    private AdminUserQueryResponse buildSuccessResponse(UserDO user) {
        return AdminUserQueryResponse.builder()
                .result(ResponseResult.success())
                .user(AdminUserInfoDTO.builder()
                        .id(user.getId())
                        .userId(user.getUserId())
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .createTime(user.getCreateTime())
                        .updateTime(user.getUpdateTime())
                        .build())
                .build();
    }

    private AdminUserQueryResponse buildFailResponse(String message) {
        return AdminUserQueryResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }
}
