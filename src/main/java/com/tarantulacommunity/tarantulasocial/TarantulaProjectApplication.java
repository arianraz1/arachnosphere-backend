package com.tarantulacommunity.tarantulasocial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TarantulaProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(TarantulaProjectApplication.class, args);
    }

}
