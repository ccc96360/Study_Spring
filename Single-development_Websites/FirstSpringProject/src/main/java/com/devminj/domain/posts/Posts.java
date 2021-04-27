package com.devminj.domain.posts;

import com.devminj.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity // Table과 링크될 클래스이다. 카멜케이스 이름을 언더스코어 네이밍(스네이크 케이스)으로 매칭한다.
public class Posts extends BaseTimeEntity {

    @Id // PK이다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 생성규칙, Auto Increment가 적용된다.
    private Long id;

    // 디폴트 값외에 추가로 변경하고 싶은 옵션이 있을때 사용, 없어도 테이블의 컬럼이다.
    // String의 columnDefinition의 디폴트 값은 VarChar(255)이다.
    @Column(length = 500, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    //@Column이 없어도  클래스의 필드는 어트리 뷰트이다.
    private String author;

    @Builder
    public Posts(String title, String content, String author){
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public void update(String title, String content){
        this.title = title;
        this.content = content;
    }
}
