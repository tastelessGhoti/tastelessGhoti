package com.kakaopay.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AccountPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountPlatformApplication.class, args);
    }
}
