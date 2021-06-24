package com.devminj.basic.discount;

import com.devminj.basic.member.Grade;
import com.devminj.basic.member.Member;

public class FixDiscountPolicy implements DiscountPolicy{

    private int discountFixAmount = 1000;
    @Override
    public int discount(Member member, int pirce) {
        if(member.getGrade() == Grade.VIP){
            return discountFixAmount;
        }
        else{
            return 0;
        }
    }
}
