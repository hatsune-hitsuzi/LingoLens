package com.hatsune.lingolens.common.mybatis;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Getter
@Setter
public class PageList<T> extends ArrayList<T> {

    /**
     * 当前页码，从 1 开始。
     */
    private int pageNumber;

    /**
     * 每页数量。
     */
    private int pageSize;
}
