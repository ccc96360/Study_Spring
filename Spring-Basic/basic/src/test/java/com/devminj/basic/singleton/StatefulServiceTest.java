package com.devminj.basic.singleton;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

public class StatefulServiceTest {

    @Test
    void statefulServiceSingleton(){
        ApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);
        StatefulService statefulService1 = ac.getBean(StatefulService.class);
        StatefulService statefulService2 = ac.getBean(StatefulService.class);

        //Thread A : A사용자 10000원 주문
        int userAPrice = statefulService1.order("userA", 10000);

        //Thread B : B사용자 20000원 주문
        int userBPrice = statefulService2.order("userB", 20000);

        //Thread A : A 사용자 주문 금액 조회
//        System.out.println("price = " + price);
        System.out.println("price = " + userAPrice);
        assertThat(userBPrice).isEqualTo(20000);

    }

    static class TestConfig{

        @Bean
        public StatefulService statefulService(){
            return new StatefulService();
        }
    }
}
