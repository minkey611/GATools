package com.minkey;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@EnableAutoConfiguration
@SpringBootApplication
public class DetectorRun {


    public static void main(String[] args) {

        SpringApplication.run(DetectorRun.class, args);


        log.warn("探针启动完成。");
    }



}
