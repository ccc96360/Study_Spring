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

## 2. @Query에 직접 JPQL 작성하기
### 2.1 리포지토리 메소드에 쿼리 정의
```java
    @Query("select m from Member m where m.userName = :userName and m.age = :age")
    List<Member> findUser(@Param("userName") String userName, @Param("age") int age);
```
- 위와 같이 @Query안에 바로 JPQL 을 작성할 수 있다.
- 쿼리에 오타 존재시 애플리케이션 로딩 시점에서 에러가 발생한다.

### 2.2 값이나 DTO로 조회
- 단순한 값 조회
```java
    @Query("select m.userName from Member m")
    List<String> findUserNameList();
```
- DTO로 조회
```java
@Query("select new 패키지명.DTO(m.id, m.userName, t.name) from Member m join m.team t")
    List<DTO> findDto();
```

## 3. 파라미터 바인딩
- 위치 기반
```java
select m from Member m where m.userName = ?0
```
- 이름 기반
```java
select m from Member m where m.userName = :name
```
### 3.1 컬렉션 파라미터 바인딩
```java
@Query("select m from Member m where m.userName in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);
```
- 위 와 같이 컬렉션도 가능
## 4. 반환 타입
- Spring Data JPA는 반환 타입을 유연하게 제공한다.
```java
        List<Member> findAByUserName(String userName);
        Member findBByUserName(String userName);
        Optional<Member> findCByUserName(String userName);
```
- 모두 UserName을 기반으로 Member를 찾는 쿼리이지만, 반환타입에 맞춰서 알아서 결과가 잘 나온다.
- List 조회에서 조건에 해당하는 Row가 없다면(결과가 없다면) Null이 아닌 빈 컬렉션이 반환된다. 즉 결과 == null => false임.
- 단건 조회(Member 로 리턴)은 결과가 없다면 null을 리턴해줌. JPA는 getSingleResult로 값을 조회하면 NoResultException이 발생하는 것과 비교된다.
- 단건 조회에서 결과가 2개 이상이면 Exception 발생