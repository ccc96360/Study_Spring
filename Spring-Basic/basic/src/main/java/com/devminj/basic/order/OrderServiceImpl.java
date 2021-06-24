package com.devminj.basic.order;

import com.devminj.basic.discount.DiscountPolicy;
import com.devminj.basic.discount.FixDiscountPolicy;
import com.devminj.basic.member.Member;
import com.devminj.basic.member.MemberRepository;
import com.devminj.basic.member.MemoryMemberRepository;

public class OrderServiceImpl implements OrderService{

    private final MemberRepository memberRepository = new MemoryMemberRepository();
    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);

        return new Order(memberId, itemName, itemPrice, discountPrice);
    }
}
