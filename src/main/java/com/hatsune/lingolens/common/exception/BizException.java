package com.hatsune.lingolens.common.exception;

/**
 * Business exception exposed to upper HTTP/RPC layers.
 *
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
