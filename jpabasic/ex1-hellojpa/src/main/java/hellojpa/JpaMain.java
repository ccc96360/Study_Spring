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

            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member = new Member();
            member.setName("Hello");
            member.setTeam(team);

            em.persist(member);

            em.flush();
            em.clear();

            Member m = em.find(Member.class, member.getId());

            System.out.println("name = " + m.getName());

            System.out.println("==================================");
            System.out.println("m = " + m.getTeam().getClass());
            System.out.println("==================================");

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
