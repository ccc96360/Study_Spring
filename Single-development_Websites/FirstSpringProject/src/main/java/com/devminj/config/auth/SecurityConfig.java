package com.devminj.config.auth;

import com.devminj.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

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
