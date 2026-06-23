package com.hatsune.lingolens.module.lens.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LensGroundingResponse {

    private ResponseResult result;

    private List<LensGroundingNodeDTO> nodes;
}
