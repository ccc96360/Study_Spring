package com.devminj.basic;

import com.devminj.basic.member.Grade;
import com.devminj.basic.member.Member;
import com.devminj.basic.member.MemberService;
import com.devminj.basic.member.MemberServiceImpl;
import com.devminj.basic.order.Order;
import com.devminj.basic.order.OrderService;
import com.devminj.basic.order.OrderServiceImpl;

public class OrderApp {
    public static void main(String[] args) {
        MemberService memberService = new MemberServiceImpl();
        OrderService orderService = new OrderServiceImpl();

        Long memberId = 1L;
        Member member = new Member(memberId, "memberA", Grade.VIP);
        memberService.join(member);

        Order order = orderService.createOrder(memberId, "itemA", 10000);
        System.out.println("order = " + order);
        System.out.println("calculate order = " + order.calculatePrice());

    }
}
