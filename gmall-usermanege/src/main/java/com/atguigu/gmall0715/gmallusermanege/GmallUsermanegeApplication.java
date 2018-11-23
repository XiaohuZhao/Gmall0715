package com.atguigu.gmall0715.gmallusermanege;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.atguigu.gmall0715.**.mapper")
public class GmallUsermanegeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallUsermanegeApplication.class, args);
    }
}
