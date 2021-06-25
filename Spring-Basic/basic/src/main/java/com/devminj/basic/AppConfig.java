package com.devminj.basic;

import com.devminj.basic.discount.FixDiscountPolicy;
import com.devminj.basic.member.MemberService;
import com.devminj.basic.member.MemberServiceImpl;
import com.devminj.basic.member.MemoryMemberRepository;
import com.devminj.basic.order.OrderService;
import com.devminj.basic.order.OrderServiceImpl;

public class AppConfig {

    public MemberService memberService(){
        return new MemberServiceImpl(new MemoryMemberRepository());
    }

    public OrderService orderService(){
        return new OrderServiceImpl(new MemoryMemberRepository(), new FixDiscountPolicy());
    }
}