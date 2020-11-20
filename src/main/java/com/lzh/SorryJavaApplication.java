package com.lzh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class SorryJavaApplication extends SpringBootServletInitializer {
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(SorryJavaApplication.class);// 注意 ：这个类必须是启动类的名称
    }

	public static void main(String[] args) {
		SpringApplication.run(SorryJavaApplication.class, args);
	}
}
