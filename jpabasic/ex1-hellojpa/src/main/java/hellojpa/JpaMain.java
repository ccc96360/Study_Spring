package hellojpa;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();
        try {
            Member member= new Member();
            member.setName("member1");
            member.setHomeAddress(new Address("city1", "Street", "zip"));

            member.getFavoriteFoods().add("치킨");
            member.getFavoriteFoods().add("피자");
            member.getFavoriteFoods().add("족발");

            member.getAddressHistory().add(new AddressEntity("old1", "street1", "zip1"));
            member.getAddressHistory().add(new AddressEntity("old2", "street2", "zip2"));

            em.persist(member);

            em.flush();
            em.clear();

            System.out.println("=================== 조회 ====================");
            Member findMember = em.find(Member.class, member.getId());

            findMember.setHomeAddress(new Address("new city", "new Street", "new zip"));

            findMember.getFavoriteFoods().remove("족발");
            findMember.getFavoriteFoods().add("한식");

            findMember.getAddressHistory().remove(new AddressEntity("old1", "street1", "zip1"));
            findMember.getAddressHistory().add(new AddressEntity("city1", "Street", "zip"));
            tx.commit();
        } catch (Exception e){
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.clear();
        }
        emf.close();
    }
}
