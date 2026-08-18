package com.anoop.videoStream.metadata;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;



@Component
public class test {
    @Value("${jwt.secret}")
    private String testSecret;

    @PostConstruct
    public void test() {
        System.out.println(
                "JWT SECRET LOADED: " +
                        (testSecret != null && !testSecret.isBlank()));
    }
}
