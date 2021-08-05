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
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setName("member1");
            member.chageTeam(team);
            em.persist(member);

//            em.flush();
//            em.clear();

            // 조회
            Member findMember = em.find(Member.class, member.getId());

            List<Member> findMembers = findMember.getTeam().getMembers();

            System.out.println("================");
            for (Member m : findMembers) {
                System.out.println("m.getName() = " + m.getName());
            }
            System.out.println("================");

            tx.commit();
        } catch (Exception e){
            tx.rollback();
        } finally {
            em.clear();
        }
        emf.close();
    }
}
