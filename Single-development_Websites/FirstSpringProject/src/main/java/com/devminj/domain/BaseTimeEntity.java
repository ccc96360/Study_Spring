package com.devminj.domain;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass // JPA Entity 클래스들이 BaseTimeEntity를 상속할 경우 필드를도 칼럼으로 인식하도로 한다.
@EntityListeners(AuditingEntityListener.class) // Auditing 기능을 포함시킨다.
public class BaseTimeEntity {

    @CreatedDate // Entity가 생성되어 저장될 때 시간이 자동 저장된다.
    private LocalDateTime createDate;

    @LastModifiedDate // Entity의 값을 변경할 때 시간이 저장 된다.
    private LocalDateTime modifiedDate;
}
