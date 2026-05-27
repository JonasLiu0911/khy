package com.example.ky.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KyTrajectoryAbnormalApplication {
    public static void main(String[] args) {
        SpringApplication.run(KyTrajectoryAbnormalApplication.class, args);
    }
}
