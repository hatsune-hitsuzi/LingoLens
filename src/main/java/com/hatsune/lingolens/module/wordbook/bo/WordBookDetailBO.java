package com.hatsune.lingolens.module.wordbook.bo;

import com.hatsune.lingolens.module.wordbook.domain.WordBookDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordBookDetailBO {

    /**
     * 词库 DO 详情。
     */
    private WordBookDO wordBook;
}
