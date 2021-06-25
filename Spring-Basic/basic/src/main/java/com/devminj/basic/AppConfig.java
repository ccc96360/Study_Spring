package com.devminj.basic;

import com.devminj.basic.discount.DiscountPolicy;
import com.devminj.basic.discount.FixDiscountPolicy;
import com.devminj.basic.member.MemberRepository;
import com.devminj.basic.member.MemberService;
import com.devminj.basic.member.MemberServiceImpl;
import com.devminj.basic.member.MemoryMemberRepository;
import com.devminj.basic.order.OrderService;
import com.devminj.basic.order.OrderServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public MemberService memberService(){
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public OrderService orderService(){
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    @Bean
    public DiscountPolicy discountPolicy(){
        return new FixDiscountPolicy();
    }
}