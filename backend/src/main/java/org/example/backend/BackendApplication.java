package org.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("org.example.backend.mapper")
@SpringBootApplication(excludeName = "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
