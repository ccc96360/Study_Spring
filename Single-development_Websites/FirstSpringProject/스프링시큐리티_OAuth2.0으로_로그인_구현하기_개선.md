[이전으로](../Readme.md)
# 소셜 로그인(구글) 개선 하기
#### [소셜 로그인 구현하기](./스프링시큐리티_OAuth2.0으로_로그인_구현하기_기초.md) 에 이어서 코드를 개선해 본다.
#### 네이버 소셜 로그인 기능을 추가한다.

---

## 1. 어노테이션 기반으로 개선하기
* IndexController에서 세션값을 가져오는 부분이 세션값이 필요할때 마다 반복된다.
* 이렇게 반복되는 부분을 메소드 인자로 바로 세션값을 바로 받을 수 있도록 변경한다.(메소드 내부가 아닌)
### 1.1 @LoginUser 생성
* config/auth 패키지에 LoguinUser 어노테이션을 생성한다.
```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}
```
* Target이 Parameter이므로 메서드의 파라미터로 선언된 객체에만 붙힐수있다.
* Retention이 Runtime이므로 런타임에도 메모리에 살아있다.

### 1.2 LoginUserArgumentResolver 생성
* 같은 위치에 LoginuserArgumnetResolver 클래스를 생성한다.
* ```HandlerMethodArgumentResolver``` 인터페이스를 구현한 클래스이다.
* ```HandlerMethodArgumentResolver```는  조건에 맞는 경우 메소드가 있다면 인터페이스의 구현체가 지정한 값으로 해당 메소드의 파라미터로 넘길수 있다.
* 다음과같이 작성한다.
```java
@RequiredArgsConstructor
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final HttpSession httpSession;

    @Override
    //컨트롤러 메서드의 특정 파라미터를 지원하는지 판단한다.
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isLoginUserAnnotation = parameter.getParameterAnnotation(LoginUser.class) != null;
        boolean isUserClass = SessionUser.class.equals(parameter.getParameterType());
        // LoginUser 어노테이션이 붙어 있는지 확인하고, 파라미터가 SessionUser 이면 True 다.
        return isLoginUserAnnotation && isUserClass;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return httpSession.getAttribute("user"); //위 조건이 True 이면 세션에서 User객체를 반환한다. 
    }
}
```
* 이제 @LoginUser를 사용할 환경을 완성했다.
* @LoginUser를 사용하기위해 ```WebMvcConfigurer```에 추가한다.
* config 패키지에 WebConfig 클래스를 생성해 다음과 같이 작성한다.
```java
@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LoginUserArgumentResolver loginUserArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}
```
* ```HandlerMethodArgumentResolver```는 항상 ```WebMvcConfigurer```의 ```addArgumentResolvers()```메서드를 통해 추가해야한다.
### 1.3 반복 제거
* IndexController 에서 반복되는 부분을 ```@LoginUser``` 로 개선한다.
```java
@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;
    private final HttpSession httpSession;

    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user){
        model.addAttribute("posts", postsService.findAllDesc());

        if(user != null){
            model.addAttribute("userName", user.getName());
        }

        return "index";
    }

    ... 생략 ...
}
```
* 이제 어떤 컨트롤러든지 @LoginUser SessionUser를 파라미터로 추가하면 세션 정보를 가져올 수 있다.

---
## 2. 세션 저장소로 DB 사용하기

### 2.1 어플리케이션 재실행시 로그인이 풀리는 이유
* 세션이 내장 톰캣의 메모리에 저장되기 때문이다.
* 기본적으로 세션은 WAS의 메모리에서 저장되고 호출 된다.
* 2대 이상의 서버에서 서비스하고 있다면 톰캣마다 세션 동기화 설정을 해야 한다.

### 2.2 세션 저장소 종류
> **톰캣 세션을 사용.**
> * 2대 이상의 WAS가 구동되는 환경에서는 톰캣들 간의 세션 공유를 위한 추가 설정이 필요하다.
  
> **MySQL과 같은 DB를 세션 저장소로 사용** 
> * 여러 WAS간 도용 세션을 사용할 수 있는 가장 쉬운 방법이다.
> * 로그인 요청마다 DB I/O 가 발생해 성능상 이슈가 발생할 수 있다.
> * 보통 로그인 요청이 많이 없는 백오피스, 사내 시스템 용도에서 사용한다.
  
> **Redis, Memcached와 같은 메모리 DB를 세션 저장소로 사용**
> * B2C(Business to Consumer) 서비스에서 가장 많이 사용하는 방식이다.
> * 실제 서비스로 사용하기 위해서는 Embedded Redis와 같은 방식이 아닌 외부 메모리 서버가 필요하다.
  
* 여기서는 2번째 방법 ```DB를 세션 저장소```로 사용한다.

### 2.3 설정
* build.gradle에 다음과 같이 ```spring-session-jdbc``` 의존성을 등록한다.
```gradle
compile('org.springframework.session:spring-session-jdbc')
```
* 다음으로 ```application.properties```에 다음과 같이 추가한다.
```properties
spring.session.store-type=jdbc
```

### 2.4 테스트

* 로그인 후  h2-console로 DB를 확인하면 다음과 같이 세션을 위한 테이블 2개(```SPRING_SESSION```, ```SPRING_ATTRIBUTES```)가 생성된것을 볼 수있다.
* 이는 JPA로 인해 세션 테이블이 자동 생성되었기 때문이다.
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth2/oauth2_2.4_1.PNG)
  
* 지금도 동일하게 재시작 하면 세션이 풀린다.
* 이는 H2가 재시작 되기때문이다.
* 추후 AWS의 RDS를 사용하면 해결된다.

---
## 3. 네이버 로그인 추가
* [네이버 오픈 API](https://developers.naver.com/apps/#/register?api=nvlogin)
* **네아로(네이버 아이디로 로그인)** 서비스를 등록한다.

### 3.1 환경 설정
* 서비스 등록후 받은 Client ID와 Client Secret을 ```application-oauth.properties```에 등록한다.
* 네이버는 스프링 시큐리트르 ㄹ공식 지원하지 않기 때문에 그동안 ```CommonOAuth2Provider```에서 해주던 값들도 전부 수동으로 입력해야한다.
* 다음과 같이 작성한다.
```properties
# registration
spring.security.oauth2.client.registration.naver.client-id= 클라이언트 ID
spring.security.oauth2.client.registration.naver.client-secret= 클라이언트 Secret
spring.security.oauth2.client.registration.naver.redirect-uri={baseUrl}/{action}/oauth2/code/{registrationId}
spring.security.oauth2.client.registration.naver.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.naver.scope=name,email,profile_image
spring.security.oauth2.client.registration.naver.client-name=Naver

# provider
spring.security.oauth2.client.provider.naver.authorization-uri=https://nid.naver.com/oauth2.0/authorize
spring.security.oauth2.client.provider.naver.token-uri=https://nid.naver.com/oauth2.0/token
spring.security.oauth2.client.provider.naver.user-info-uri=https://openapi.naver.com/v1/nid/me
spring.security.oauth2.client.provider.naver.user-name-attribute=response
```
* 네이버 API의 로그인 결과는 다음과 같은 JSON으로 온다.
```json
{
  "resultcode": 00,
  "message": "success",
  "response": {
    "email": ...,
    "nickname": ...,
    "profile_image": ...,
    "age": ...,
    "gender": F,
    "id":...,
    "name": ...,
    "birthday": ...,
  }
}
```
* 스프링 시큐리티는 하위 필드를 명시할수 없고 최상위 필드들만 user_name으로 지정 가능하다.
* 즉 resultCode, message, response 뿐이다.
* 따라서 위의 설정에서 ```spring.security.oauth2.client.provider.naver.user_name_attribute=response```와 같이 response를 user_name으로 설정한다.
* 이후 자바코드로 ```response```의 ```id```를 user_name으로 지정한다.

### 3.2 스프링 시큐리티 설정 등록
* 구글 로그인을 등록하면서 코드를 확장성 있게 작성해서 네이버는 쉽게 등록 가능하다.
* ```OAuthAttributes```에 다음과 같이 네이버인지 판단하는 코드와 네이버 생성자만 추가하면 된다.
```java
@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;

    ... 생략 ...
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes){
        if("naver".equals(registrationId)){
            return ofNaver("id", attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }
    ... 생략 ...
    
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes){
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("picture"))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .build(); 
    }
    ... 생략 ...
}
```
### 3.3 네이버 로그인 버튼 추가
* index.mustache에 네이버 로그인 버튼을 다음과 같이 추가한다.
```html
{{^userName}}
    <a href="/oauth2/authorization/google" class="btn btn-success active">Google Login</a>
    <a href="/oauth2/authorization/naver" class="btn btn-success active">Naver Login</a>
{{/userName}}
```
* 위 와 같이 구글 로그인 밑에 한줄 작성해 준다.
* 네이버 로그인 URL은 ```application-oauth.properties```에 등록한 redirect-uri값에 맞춰 자동으로 등록된다.

### 3.4 테스트
* 실행 시켜보면 다음과 같이 네이버 로그인 버튼이 추가 되었다.
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth2/oauth2_3.4_1.PNG)

* 다음과 같이 로그인이 정상적을 된것을 확인 할 수 있다.
* 로그인 후 이전 글에서는 이름이 로컬PC 이름이 출력됐었다.
* 이는 IndexController에서 model에 등록한 attribute 이름이 userName이였는데 userName으로 사용해서 발생하는 문제였다 이를 nameOfUser와 같이  수정하면 정상적으로 출력 가능하다.

![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth2/oauth2_3.4_2.PNG)
  
* DB에도 정상적으로 등록 되었다.

![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth2/oauth2_3.4_3.PNG)
  

---
## 4. 기존 테스트에 시큐리티 적용하기.
* 현재 로그인 기능을 추가하면서 인증된 사용자만 API를 호출할 수 있는 상황이다.
* 그래서 IntelliJ 우측 Gradle탭에서 ```verification``` 내부에 Test를 실행해보면 모두 실패한다.
* 전체 실행시 ```Execution failed for task ':test'.``` 에러가 발생 했다면 Junit4는 build.gradle에 다음과 같은 설정이 있으면 안된다고 한다.
```gradle
test {
    useJUnitPlatform()
}
```
* 그냥 test 디렉토리 우클릭 해서 Run 'All Tests'하는게 더 편한것 같다.
* 따라서 테스트 코드마다 인증한 사용자가 호출한 것처럼 작동하도록 수정해야한다.

### 4.1 CustomOAuth2UserService를 찾을 수 없음 해결
* ```CustomOAuth2UserService```를 생성하는데 필요한 소셜 로그인 관련 설정값들이 없기 때문에 발생한다.
* ```application-oauth.properties```에 설정 값들을 추가 했지만 ```src/main```과 ```src/test```환경이 다르기 때문이다.
* test에 ```application.properties```가 없으면 main 에서 가져오지만 ```application-oauth.properties```는  가져오지 않는다.
* 그래서 이문제를 해결하기위해 test에 ```application.properties```를 만든다.

### 4.2 임의로 인증된 사용자 추가하기
* ```Posts_등록된다``` 테스트의 상태 코드가 302(리다이렉션 응닫)로 실패했다.
* 이는 스프링 시큐리티에서 ```인증되지 않은 사용자의 요청은 이동```시키기 때문이다.
* 임의로 인증된 사용자를 추가시키기위해 ```spring-security-test```를 다음과 같이 ```build.gradle``` 에 추가한다
```gradle
testCompile('org.springframework.security:spring-security-test')
```
* 다음으로 PostsApiControllerTest의 2개 테스트 메소드에 다음과 같이 임의의 사용자 인증을 추가한다.
```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {
    ... 생략 ...

    @Test
    @WithMockUser(roles = "USER")
    public void Posts_등록된다() throws Exception{
        ... 생략 ...
    }

    @Test
    @WithMockUser(roles = "USER")
    public void Posts_수정된다() throws Exception{
        ... 생략 ... 
    }
}
```
* ```@WithMockUser```를 통해 roles 권한을 가진 가짜 사용자를 만들 수 있다.
* 이 상태로는 아직 작동을 하지 않는데, 이유는 ```@WithMockUser```는 ```MockMvc```에서만 작동하기 때문이다.
* 현재 PostsApiControllerTest는 @SpringBootTest 로만 되어있어 MockMvc를 전혀 사용하지 않는다.

### 4.3 @SpringBootTest에서 MockMvc 사용하기
* PostsApicContollerTest를 다음과 같이 수정한다
```java
package com.devminj.web;

import com.devminj.domain.posts.Posts;
import com.devminj.domain.posts.PostsRepository;
import com.devminj.web.dto.PostsSaveRequestDto;
import com.devminj.web.dto.PostsUpdateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {

    ... 생략 ...
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup(){
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void Posts_등록된다() throws Exception{
        // given
        ... 생략 ..
        // when
        mvc.perform(post(url)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(new ObjectMapper().writeValueAsString(requestDto)))
            .andExpect(status().isOk());

        //ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, requestDto, Long.class);
       
        // then
        //assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        //assertThat(responseEntity.getBody()).isGreaterThan(0L);
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void Posts_수정된다() throws Exception{
        //given
        ... 생략 ...
        //when
        mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());
        //then
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);
        assertThat(all.get(0).getAuthor()).isEqualTo("author");

        Posts post = all.get(0);
        System.out.println(">>>>>>>>> createDate = " + post.getCreateDate() + ", modifiedDate = " + post.getModifiedDate());
    }
}
```
* 매 테스트 시작전 MockMvc 인스턴스를 생성한다.
* 생성된 MockMvc를 통해 API를 테스트한다.

### 4.3 @WebMvcTest에서 CustomAuth2UserService를 찾을 수 없음
* HelloControllerTest는 @WebMvcTest를 사용한다.
* 스프링 시큐리티 설정은 잘 작동하지만, @WebMvcTest는 CustomOAuth2UserService를 스캔하지 않는다.
* 왜냐하면, @WebMvcTest는 WebSecurityConfigurerAdapter, WebMvcConfigurer를 비롯한 @ControllerAdvice, @Controller를 읽는다.
* 즉, @Repository, @Service, @Component는 스캔 대상이 아니다.
* 따라서 SecurityConfig는 읽었지만, SecurityConfig를 생성하기위한 CustomOAuth2UserService는 읽을 수 가없어서 에러가 발생한다.
* 그래서 다음과 같이 스캔대상에서 SecurityConfig를 제거한다.
```java
@WebMvcTest(controllers = HelloController.class,
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
public class HelloControllerTest {
    ... 생략 ...
}
```
* 다음으로 @WithMockUser를 사용해 가짜로 인증된 사용자를 생성한다.
```java
@RunWith(SpringRunner.class) // 스프링 부트 테스트와 JUnit 사이에 연결자 역할을 한다.
/*Web(Spring MVC)에 집중할 수 있는 어노테이션
* @Controller @ControllerAdvice 등을 사용할 수 있다.
* @Service @Component @Repository 등은 사용할수 없다.
* */
@WebMvcTest(controllers = HelloController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
public class HelloControllerTest {
    
    @WithMockUser(roles = "USER")
    @Test
    public void hello가_리턴된다() throws Exception{
        ... 생략 ...
    }

    @WithMockUser(roles = "USER")
    @Test
    public void helloDto가_리턴된다() throws Exception{
        ... 생략 ...
    }

}
```
* 그런데 이렇게 수정하면 다음과 같은 에러가 발생한다.
```aidl
Error creating bean with name 'jpaMappingContext': Invocation of init method failed; nested exception is java.lang.IllegalArgumentException: JPA metamodel must not be empty!
```
* 이 에러는 @EnableJpaAuditing 으로 인해 발생한다.
* 위 어노테이션을 사용하기 위해선 최소 하나의 @Entity 클래스가 필요하다. 
* @WebMvcTest에는 당연히 없다.
* @SpringBootApplication 과 @EnableJpaAuditing 이 같이 있어 발생하는 에러이므로 서로 분리 시켜준다.
* 먼저, Application.java에서 @EnableJpaAuditing 을 삭제한다.
* 다음으로 config 패키지에 JpaConfig를 생성해 다음과 같이 작성한다.
```java
@Configuration
@EnableJpaAuditing
public class JpaConfig { }

```
* 위의 모든 과정을 마치면 아래와 같이 모든 테스트가 통과하는 것을 볼 수 있다. 
  
![이미지](https://raw.githubusercontent.com/ccc96360/ccc96360/main/images/studyspring/oauth2/oauth2_4.3_1.PNG)

---