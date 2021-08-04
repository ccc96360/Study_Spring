package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();
        try {
            // 비영속
            Member member = new Member();
            member.setId(101L);
            member.setName("HelloJPA");

            //영속
            System.out.println("=== Before ===");
            em.persist(member);
            System.out.println("=== After  ===");

            // 1차 캐시에서 데이터를 가져 오기 때문에 select Query 안나감
            Member findMember = em.find(Member.class, 101L);

            System.out.println("findMember id = " + findMember.getId());
            System.out.println("findMember name = " + findMember.getName());

            Member findMember2 = em.find(Member.class, 1L);
            // 1차 캐시에서 데이터를 가져 오기 때문에 select Query 안나감
            Member findMember3 = em.find(Member.class, 1L);

            //영속 엔티티의 동일성 보장
            System.out.println("findMember2 == findMember3 : " + (findMember2 == findMember3));

            // commit 될 때 insert 쿼리 나감. (쓰기 지연),
            // em.flush()를 호출하면 호출 시점에 insert 쿼리 나감. (1차 캐시는 유지, 쓰기 지연 SQL 저장소 쿼리 들이 DB에 적용됨.)
            tx.commit();
        } catch (Exception e){
            tx.rollback();
        } finally {
            em.clear();
        }
        emf.close();
    }
}
