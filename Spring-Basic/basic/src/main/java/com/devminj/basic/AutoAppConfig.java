package com.devminj.basic;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
//        basePackages = "com.devminj.basic.member", => 이 패키지 부터 하위 패키지 탐색함
//        basePackageClasses = AutoAppConfig.class, => 이 클래스 부터 탐색함
//        위 설정 들을 지정하지 않으면 이 클래스의 패키지가 시작 위치이다.
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
)
public class AutoAppConfig {
}
