package com.hatsune.lingolens;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@MapperScan({
        "com.hatsune.lingolens.module.user.mapper",
        "com.hatsune.lingolens.module.admin.mapper",
        "com.hatsune.lingolens.module.memorypalace.mapper",
        "com.hatsune.lingolens.module.arscan.mapper",
        "com.hatsune.lingolens.module.wordasset.mapper",
        "com.hatsune.lingolens.module.review.mapper"
})
@EnableAsync
@SpringBootApplication
public class LingoLensApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingoLensApplication.class, args);
    }

}
