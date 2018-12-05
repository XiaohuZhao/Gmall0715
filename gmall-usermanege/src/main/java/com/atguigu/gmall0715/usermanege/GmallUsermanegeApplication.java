package com.atguigu.gmall0715.usermanege;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.atguigu.gmall0715.**.mapper")
@ComponentScan("com.atguigu.gmall0715")
public class GmallUsermanegeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallUsermanegeApplication.class, args);
    }
}
