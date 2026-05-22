package com.hatsune.lingolens;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@MapperScan({
        "com.hatsune.lingolens.module.user.mapper",
        "com.hatsune.lingolens.module.wordbook.mapper",
        "com.hatsune.lingolens.module.admin.mapper",
        "com.hatsune.lingolens.module.memorypalace.mapper"
})
@SpringBootApplication
public class LingoLensApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingoLensApplication.class, args);
    }

}
