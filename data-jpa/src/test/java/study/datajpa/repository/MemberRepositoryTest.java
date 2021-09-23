package study.datajpa.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

    @BeforeEach
    public void cleanUp(){
        memberRepository.deleteAll();
    }


    @Test
    public void testMember(){
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(savedMember.getId());
        assertThat(findMember.getUserName()).isEqualTo(savedMember.getUserName());
        assertThat(findMember).isEqualTo(findMember);
    }

    @Test
    public void basicCRUD() throws Exception{
        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);
        //when
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        List<Member> all = memberRepository.findAll();

        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long count = memberRepository.count();

        //then

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        assertThat(all.size()).isEqualTo(2);

        assertThat(count).isEqualTo(0L);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen() throws Exception{
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        //when
        List<Member> res = memberRepository.findByUserNameAndAgeGreaterThan("AAA", 15);

        //then

        assertThat(res.get(0).getUserName()).isEqualTo("AAA");
        assertThat(res.get(0).getAge()).isEqualTo(20);
        assertThat(res.size()).isEqualTo(1);

    }

    @Test
    public void testNamedQuery() throws Exception{
        //given

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);
        //when

        List<Member> res = memberRepository.findByUserName("BBB");
        //then

        Member member = res.get(0);

        assertThat(member).isEqualTo(m2);
    }

    @Test
    public void testQuery(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> res = memberRepository.findUser("AAA", 10);
        Member findMember = res.get(0);

        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void findUserNameList(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> userNameList = memberRepository.findUserNameList();
        for (String s : userNameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto(){
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);


        List<MemberDto> dtos = memberRepository.findMemberDto();
        for (MemberDto dto : dtos) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findUserNames(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> res = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : res) {
            System.out.println("member = " + member);;
        }
    }

    @Test
    public void returnType(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> aaa = memberRepository.findAByUserName("AAA");
        Member aaa1 = memberRepository.findBByUserName("AAA");
        Optional<Member> aaa2 = memberRepository.findCByUserName("AAA");

        System.out.println(aaa);
        System.out.println(aaa1);
        System.out.println(aaa2);
    }
}
