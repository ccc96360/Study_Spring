package hellojpa;

import javax.persistence.*;

@Entity
public class Locker {

    @Id @GeneratedValue
    @Column(name = "LOCKER_ID")
    private Long id;

    private String name;

    // 1 대 1 양방향 매핑을 위함
    @OneToOne(mappedBy = "locker")
    private Member member;
}
