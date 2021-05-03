[이전으로](../Readme.md)
# 소셜 로그인(구글) 구현하기
#### Spring Security 와 Oauth2로 소셜 로그인 기능을 구현해 본다.

---
###  스프링 부트 1.5 vs 스프링 부트 2.0
* OAuth2 연동 방법이 스프링 부트 2.0에서는 크게 변경되었다.
* 하지만, 인터넷 자료들을 보면 설정 방법에 크게 차이가 없는 경우가 자주보인다.
* 이는 ```spring-security-oauth2-autoconfigure```라이브러리 덕분이다.
* 위 라이브러리를 사용할 경우 1.5에서 쓰던 설정을 그대로 사용 가능하다.
* 하지만 여기선 새로운 방식을 사용한다.

---
### Spring Security OAuth2 Client 라이브러리를 사용하는 이유
* 기존 1.5에서 사용되던 라이브러리는 신규 기능이 추가되지 않는다.
* 스프링 부트용 라이브러리가 출시 됐다.
* 기존 방식은 확장 포인트가 적절하게 오픈 되어있지 않아 직접 상속하거나 오버라이딩 해야하지만 2.0의 경우 확장 포인트를 고려해 설계된 상태이다.

---
## 1. 구글 서비스 등록

### 1.1 구글에서 OAuth 클라이언트 ID 생성
* [구글 클라우드 플랫폼](https://console.cloud.google.com/?pli=1) 으로 이동한다.
* 새로운 프로젝트를 만든다.
* 사용자 인증 정보에서 OAuth 클라이언트 ID를 생성한다.

### 1.2 앱에서 설정하기
* ```application.properties```가 있는 위치(src/main/resources)에 ```application-oauth.properties```파일을 생성한다
* 다음과 같이 코드를 추가한다
```properties
spring.security.oauth2.client.registration.google.client-id= 클라이언트 ID
spring.security.oauth2.client.registration.google.client-secret= 클라이언트 보안 비밀
spring.security.oauth2.client.registration.google.scope=profile,email
```
* 새로 추가한 ```application-oauth.properties```를 기본설정 파일(```application.properties```)에 포함시킨다.
```properties
... 생략 ...
spring.profiles.include=oauth
```
* ```application-oauth.properties```를 공개해 버리지 않도록 ```.gitignore``` 에 꼭 추가해야한다.

---
## 2. 구글 로그인 연동

### 2.1 User 클래스 생성
* 사용자 정보를 담당할 도메인 이다.
* domain 패키지 내부에 user 패키지를 생성후 내부에 ```User``` 클래스를 다음과 같이 작성한다.
```java
@Getter
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column
    private String picture;

    @Enumerated(EnumType.STRING) //  JPA로 DB에 저장할때 Enum값을 어떤 형태로 저장할지를 결정한다.
                                // 디폴트는 int로 된 숫자이다.
                                // 숫자로 저장되면 그 값이 무슨 코드르 의미하는지 알 수가 없다.
    @Column(nullable = false)
    private Role role;

    @Builder
    public User(String name, String email, String picture, Role role) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
    }

    public User update(String name, String picture){
        this.name = name;
        this.picture = picture;

        return this;
    }

    public String getRoleKey(){
        return this.role.getKey();
    }
}
```
### 2.2 Role 클래스 생성
* 각 사용자의 권한을 관리할 Enum 클래스 ```Role```를 생성한다.
```java
@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("ROLE_GUEST", "손님"),
    USER("ROLE_USER", "일반 사용자");
    
    private final String key;
    private final String title;
}
```
* 스프링 시큐리티에서는 ```권한 코드```에 항상 ```ROLE_```이 앞에 있어야만 한다.

### 2.3 UserRepository 생성
* CRUD를 책임져줄 ```UserRepository``` 를 다음과 같이 생성한다.
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

---
## 3. 스프링 시큐리티 설정
* ```build.gradle``` 에 다음과 같이 의존성을 추가한다.
```gradle
compile('org.springframework.boot:spring-boot-starter-oauth2-client')
```
### 3.1 설정 코드 작성
* config.auth 패키지를 생성한다.
* 앞으로 시큐리티 관련 클래스는 모두 이 패키지에서 관리한다.
* 다음과 같이 ```SecurityConfig```클래스를 작성한다.
```java
@RequiredArgsConstructor
@EnableWebSecurity // 스프링 시큐리티 설정들을 활성화 시켜준다.
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()//h2-console 화면을 사용하기위해 disable 한다.
                .headers().frameOptions().disable() //h2-console 화면을 사용하기위해 disable 한다.
                .and()
                    .authorizeRequests() // URL별 권한 관리를 설정하는 옵션의 시작점이다.
                                         // authorizeRequests()이 선언되어야만 antMatchers옵션을 사용할 수 있다.
                    .antMatchers("/","/css/**","images/**","/js/**","/h2-console").permitAll()
                    .antMatchers("/api/v1/**").hasRole(Role.USER.name()) //USER권한을 가진 사람만 접근할 수 있다.
                    .anyRequest()/*위에 설정된 값들 이외 나머지 URL들이다.*/.authenticated()// 인등된 사용자들(로그인한 사용자들)만 접근 할 수 있다. 
                .and()
                    .logout()
                        .logoutSuccessUrl("/") // 로그아웃 성공시 루트로 이동
                .and()
                    .oauth2Login()
                        .userInfoEndpoint() // 로그인 성공 이후 사용자 정보를 가져올 때의 설정들을 담당한다.
                            .userService(customOAuth2UserService); // 로그인 성공시 후속 조치를 진행할 UserService 인터페이스의 구현체를 등록한다.
                                                                   // 리소스 서버(구글, 네이버 등) 에서 사용자 정보를 가져온 상태에서 추가로 진행하고자 하는 기능을 명시할 수 있다.
    }
}
```
* 다음으로 ```CustomOAuth2UserService```클래스를 작성한다.
```java
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 로그인 진행중인 서비스를 구분하느 코드이다.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // OAuth2 로그인 진행 시 키가되는 필드 값을 이야기한다. Primary Key와 같은 의미이다.
        // 구글은 기본적으로 코드를 지원하지만 네이버 카카오 등은 기본 지원하지 않는다.
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        // OAuth2UserService를 통해 가져온 OAuth2User의 Attribute를 담는 클래스이다.
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);

        httpSession.setAttribute("user", new SessionUser(user));// SessionUser는 세션에 사용자 정보를 저장하기 위한 DTO이다.

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private User saveOrUpdate(OAuthAttributes attributes){
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());
        return userRepository.save(user);

    }
}
```
* 다음으로 ```OAuthAttributes``` 클래스를 작성한다.
* config.auth 패키지 내부에 dto 패키지를 생성해 그 안에 생성한다.
```java
@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes){
        return ofGoogle(userNameAttributeName, attributes);
    }
    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }
    public User toEntity(){
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.GUEST)
                .build();
    }
}
```
* ```toEntity()```메서드는 최초 가입시(로그인 시)실행된다. 즉 DB에 일치하는 Email이 없는경우 실행된다. 
* 마지막으로 세션에 사용자 정보를 저장할 DTO인 ```SessionUser``` 클래스를 생성한다.
```java
@Getter
public class SessionUser implements Serializable {
    private String name;
    private String email;
    private String picture;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
```
> 세션에서 ```User```클래스를 사용하지 않는이유
> 1. ```User``` 클래스는 직렬화 되지 않았다. (Serializable 이아니다.)
> 2. ```User``` 클래스는 엔티티 이기 떄문에 직렬화 시 연관된 엔티티들 까지 포함되기 때문에 성능이슈, 부수 효과가 발생할 확률이 높다.
> 3. 따라서 직렬화 기능을 가진 세션 DTO 를  추가로 만드느것이 운영 및 유지보수 때 더 낫다.

---
## 4. 로그인 테스트

### 4.1 UI 변경
* 로그인 기능을 추가 하기 위해 로그인 버튼을 추가한다.
* 다음과 같이 ```index.mustache```를 수정한다
```html
{{>layout/header}}
    <h1>스프링 부트~~</h1>
    <div class="col-md-12">
        <div class="row">
            <div class="col-md-6">
                <a href="/posts/save" role="button" class="btn btn-primary">글 등록</a>
                <!--로그인 기능 -->
                {{#userName}}
                    Logged in as: <span id = "user">{{userName}}</span>
                    <a href="/logout" class="btn btn-info active" role="button">Logout</a>
                {{/userName}}
                {{^userName}}
                    <a href="/oauth2/authorization/google" class="btn btn-success active">Google Login</a>
                {{/userName}}
            </div>
        </div>
    </div>
    <!-- 게시글 목록 -->
    ... 생략 ...
{{>layout/footer}}
```
* {{#변수}} (```{{#userName}}```) 은 변수 ```userName```의 값의 존재 유무를 판단한다.(존재 할 경우 True)
* {{^변수}} (```{{^userName}}```) 는 변수가 존재하지 않을 경우 참이다.
* ```/logout```은 스프링 시큐리티에서 기본적으로 제공하는 로그아웃 URL이다.
* 즉 별도로 컨트롤러를 만들 필요가 없다. ```SecurityConfig```클래스에서 변경 가능하다.
* ```href="/oauth2/authorization/google"```도 스프링 시큐리티에서 제공하는 로그인 URL이다.

### 4.2 IndexController 수정
* ```index.mustache```에서 ```userName```을 사용할 수 있게 ```IndexController``` 코드를 수정한다.
```java
@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;
    private final HttpSession httpSession;
    
    @GetMapping("/")
    public String index(Model model){
        model.addAttribute("posts", postsService.findAllDesc());

        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        if(user != null){
            model.addAttribute("userName", user.getName());
        }
        
        return "index";
    }
    ... 생략 ...
```

### 4.3 테스트
* 다음과 같이 로그인 버튼이 추가됐다.
  
![image](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth/OAuth_4.3_1.PNG)
  
* 로그인 버튼을 누르면 권한을 요청한다.
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth/OAuth_4.3_2.PNG)

* 권한을 수락하면 다음과 같이 로그인 되는것을 확일 할 수 있다.
    
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth/OAuth_4.3_3.PNG)
  
* 로그아웃 버튼을 누르면 로그아웃이 된다.
* 디비에도 회원 정보가 등록 된 것을 확인 할 수 있다.
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth/OAuth_4.3_4.PNG)
  
* 로그인시 GUEST 권한이므로 글을 작성할 수 없다.
* ```SecurityConfig```에서 ```/api/v1/```이하의 API 요청은 ```USER``` 권한 으로 제한 했기때문이다.
* 따라서 글 등록 시도시 다음과 같은 403에러(권한 거부)가 발생한다.
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth/OAuth_4.3_5_.PNG)

* DB에서 다음과 같이 권한을 ```USER```로 변경 한다.
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth/OAuth_4.3_6.PNG)

* 다음과 같이 글이 등록 되는 것을 확인 할 수 있다.
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth/OAuth_4.3_7.PNG)
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth/OAuth_4.3_8.PNG)
