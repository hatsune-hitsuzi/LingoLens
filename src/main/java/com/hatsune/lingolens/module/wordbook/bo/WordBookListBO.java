package com.hatsune.lingolens.module.wordbook.bo;

import com.hatsune.lingolens.module.wordbook.domain.WordBookDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordBookListBO {

    /**
     * 当前页码。
     */
    private Integer pageNumber;

    /**
     * 每页数量。
     */
    private Integer pageSize;

    /**
     * 符合条件的词库总数。
     */
    private Integer total;

    /**
     * 词库 DO 列表。
     */
    private List<WordBookDO> wordBooks;
}
