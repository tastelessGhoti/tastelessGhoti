package com.bookverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BookVerse 온라인 서점 플랫폼 메인 애플리케이션
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@EnableJpaAuditing
@SpringBootApplication
public class BookVerseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookVerseApplication.class, args);
    }
}
