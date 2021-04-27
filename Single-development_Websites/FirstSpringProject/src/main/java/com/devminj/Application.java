package com.devminj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// JPA Auditing 을 위한 어노테이션
@EnableJpaAuditing
// 이 어노테이션으로 인해 스프링 부트의 자동설정, 스프링Bean 읽기와 생성을 모두 자동으로 설정된다.
// 이 위치부터 설정을 읽어 가기 떄문에 항상 프로젝트의 최상단에 위치해야 한다.
@SpringBootApplication
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
