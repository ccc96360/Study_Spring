# 정리

## 1. JPA Named Query
- 실무에서 쓸일은 거의 없다.

### 1.1 기본적인 Named Query
```java
@NamedQuery(
        name = "엔티티.메서드명",
        query = "select ~~~~"
)
public class 엔티티{
    ... 생략 ... 
}

public class 엔티티Repository{
    pulbic void 함수명(){
        em.createNamedQuery("엔티티.메서드명", 엔티티.class)
                .setParameter~~~
        .~~~
    }
}
```
### 1.2 Spring Data JPA에서 Named Query
```java
public interface 엔티티repository extends JpaRepository~~~{
    @Query(name = "엔티티.메서드명")
    List<~> 메서드명(@Param("~~") param);
        
}
```
- ```엔티티명.메서드명```과 repository의 ```메서드명```을 일치시키면 @Query 가 없어도 동작한다.
- 우선순위 : NamedQuery에 일치하는 메서드명 확인 -> 메서드 이름으로 쿼리 생성