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

## 4. collection 조회를 Fetch 조인으로 최적화
- orderItems 같은 컬렉션 (List 등) 을 fetch join 하면 결과의 row가 증가한다.
- order당 orderItem이 여러개 이기 때문에
- select 절에 ```distinct``` 를 추가하면 된다.
- MySQL은 distict시 로우의 모든 값이 같아야 중복 제거가 되지만, JPA에서는 가져온 엔티티의 id가 같으면 중복이라 판단해 제거한다. #
### 단점 
- 페이징이 불가능 하다.
- Fetch Join 하면 메모리에 데이터를 다 퍼올린다음에 페이징 처리한다 따라서 데이터가 많으면 메모리가 터진다
- 데이터가 부정합하게 조회될 가능성이 있기 때문에 컬렉션 2개이상에서는 사용하면 안된다.
### 4.1 페이징과 한계 돌파
- xToOne 관계는 그냥 Fetch Join 쓰면 된다.
- 컬렉션은 지연 로딩으로 조회한다. (Fetch Join 사용 X)
- 지연 로딩 성능 최적화를 위해 ```hibernate.default_batch_fetch_size```, ```@BatchSize``` 를 적용한다
- 이 옵션을 사용하면 컬렉션이나 프록시 객체를 한꺼번에 설정한 size만큼 IN 쿼리로 조회한다.
- ```hibernate.default_batch_fetch_size``` 는 100 ~ 1000 사이을 선택 해야한다.
