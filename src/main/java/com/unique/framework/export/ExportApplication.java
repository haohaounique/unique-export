package com.unique.framework.export;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * date:2025/3/25 21:50
 * author: haohaounique@163.com
 */
@SpringBootApplication(scanBasePackages = "com.unique")
@MapperScan(value = "com.unique.framework.export.mapper")
@EnableFeignClients
@EnableDiscoveryClient
public class ExportApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExportApplication.class, args);
    }
}
