# 팁 정리

## 1. 쿼리 파라미터 로그 남기기

*  [외부라이브러리](https://github.com/gavlyukovskiy/spring-boot-data-source-decorator) 사용
*  ```implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.6'``` 추가함
* 리소스 잡아 먹으니까 운영 단계 에서는 사용을 고려해야함!

## 2. 엔티티 관련
### 2-1. 엔티티에는 가급적 Setter를 사용하지 말자.

### 2-2. 모든 연관관계는 지연로딩으로 설정 해야한다.

- 즉시로딩(Eager)은 예측이 어렵고 어떤 SQL이 실행될지 예측이 어렵다.
- 특히 JPQL을 실행할 떄 N+1 문제가 자주 발생한다.
- 연관된 엔티티를 함께 DB에서 조회해야 하면, Fetch Join 또는 엔티티 그래프 기능을 사용한다.
- XToOne(OneToOne, ManyToOne)은 기본 Fetch 전략이 Eager다. Lazy로 꼭 바꿔줘야 한다.

### 2-3 . 엔티티의 필드의 컬렉션은 필드에서 초기화 하자.
- null문제에서 안전하다.
- 하이버네이트가 엔티티를 영속화 할 때 컬렉션을 감싸 하이버네이트가 제공하느 내장 컬렉션으로 변경한다.  
이때, 임의의 메서드에서 컬렉션을 잘못 새성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다.  
  따라서 필드레벨에서 생성하는 것이 가장 안전하고, 코드도 간결하다.

## 3. Test에 application.yml이 있으면 우선권을 가진다.