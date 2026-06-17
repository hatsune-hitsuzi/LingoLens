package com.hatsune.lingolens.module.memorypalace.service;

import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceListResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceCreateResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceSearchResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceReorderResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceDeleteResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceUpdateResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordAddResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordPageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@RequestMapping("/api/memory-palace")
public interface MemoryPalaceService {

    /**
     * 接口功能: 将 Lens 或前端传入的单词信息加入当前用户的记忆宫殿。
     * 入参: requestJson，记忆宫殿加词请求 JSON 字符串，包含 sceneName、word、translation、exampleSentence 等字段。
     * 出参: MemoryPalaceWordAddResponse，包含 result、palaceId、palaceWordId、palaceWordCount、added。
     *
     * @param requestJson 加入记忆宫殿请求 JSON 字符串。
     * @return 加入结果。
     */
    @PostMapping("/word/add")
    MemoryPalaceWordAddResponse addWord(@RequestBody String requestJson);

    /**
     * 接口功能: 查询当前用户的记忆宫殿列表。
     * 入参: requestJson，分页请求 JSON 字符串，包含 pageNumber、pageSize。
     * 出参: MemoryPalaceListResponse，包含 result、分页信息和 palaces。
     *
     * @param requestJson 记忆宫殿列表请求 JSON 字符串。
     * @return 记忆宫殿列表。
     */
    @PostMapping("/list")
    MemoryPalaceListResponse list(@RequestBody String requestJson);

    @PostMapping("/create")
    MemoryPalaceCreateResponse create(@RequestBody String requestJson);

    @PostMapping("/search")
    MemoryPalaceSearchResponse search(@RequestBody String requestJson);

    @PostMapping("/reorder")
    MemoryPalaceReorderResponse reorder(@RequestBody String requestJson);

    @PostMapping("/update")
    MemoryPalaceUpdateResponse update(@RequestBody String requestJson);

    @PostMapping("/delete")
    MemoryPalaceDeleteResponse delete(@RequestBody String requestJson);

    /**
     * 接口功能: 分页查询某个记忆宫殿中的单词列表。
     * 入参: requestJson，分页请求 JSON 字符串，包含 palaceId、pageNumber、pageSize。
     * 出参: MemoryPalaceWordPageResponse，包含 result、分页信息和 words。
     *
     * @param requestJson 记忆宫殿单词分页请求 JSON 字符串。
     * @return 记忆宫殿单词分页。
     */
    @PostMapping("/word/page")
    MemoryPalaceWordPageResponse pageWords(@RequestBody String requestJson);
}
