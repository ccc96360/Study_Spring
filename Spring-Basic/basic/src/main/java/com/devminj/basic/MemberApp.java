package com.devminj.basic;

import com.devminj.basic.member.Grade;
import com.devminj.basic.member.Member;
import com.devminj.basic.member.MemberService;
import com.devminj.basic.member.MemberServiceImpl;

public class MemberApp {
    public static void main(String[] args) {
        MemberService memberService = new MemberServiceImpl();
        Member memberA = new Member(1L, "memberA", Grade.VIP);
        memberService.join(memberA);

        Member member = memberService.findMember(1L);

        System.out.println("New member " + memberA.getName());
        System.out.println("find Member = " + member.getName());
    }
}
