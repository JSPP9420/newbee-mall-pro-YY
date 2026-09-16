package com.newbee.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.newbee.mall.mapper")
public class MallProApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallProApplication.class, args);
    }
}
