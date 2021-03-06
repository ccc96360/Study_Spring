package study.datajpa.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberJpaRepositoryTest {

    @Autowired MemberJpaRepository memberJpaRepository;

    @BeforeEach
    public void cleanUp(){
        memberJpaRepository.deleteAll();
    }

    @Test
    public void testMember(){
        Member member = new Member("memberA");
        Member savedMember = memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.find(savedMember.getId());
        assertThat(member.getId()).isEqualTo(member.getId());
        assertThat(member.getUserName()).isEqualTo(member.getUserName());
    }

    @Test
    public void basicCRUD() throws Exception{
        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        //when
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();

        List<Member> all = memberJpaRepository.findAll();
        long count = memberJpaRepository.count();

        //then

        for (Member member : all) {
            System.out.println(member.getUserName() + " " + member.getId());

        }

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        assertThat(all.size()).isEqualTo(2);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    public void findByUsernameAndAgeGreaterThen() throws Exception{
        //given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);

        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);

        //when
        List<Member> res = memberJpaRepository.findByUsernameAndAgeGreaterThen("AAA", 15);

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

        memberJpaRepository.save(m1);
        memberJpaRepository.save(m2);
        //when

        List<Member> res = memberJpaRepository.findByUserName("BBB");
        //then

        Member member = res.get(0);

        assertThat(member).isEqualTo(m2);
    }

    @Test
    public void paging(){
        //given
        memberJpaRepository.save(new Member("member1", 10));
        memberJpaRepository.save(new Member("member2", 10));
        memberJpaRepository.save(new Member("member3", 10));
        memberJpaRepository.save(new Member("member4", 10));
        memberJpaRepository.save(new Member("member5", 10));

        int age = 10;
        int offset = 0;
        int limit = 3;
        //when
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        Long totalCount = memberJpaRepository.totalCount(age);

        /**
         * ????????? ?????? ?????? ??????..
         */


        //then
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5L);
    }
}