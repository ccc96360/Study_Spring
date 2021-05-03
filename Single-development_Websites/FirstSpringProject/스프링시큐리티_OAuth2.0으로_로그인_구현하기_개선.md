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