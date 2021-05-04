package com.devminj.web;

import com.devminj.config.auth.SecurityConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class) // 스프링 부트 테스트와 JUnit 사이에 연결자 역할을 한다.
/*Web(Spring MVC)에 집중할 수 있는 어노테이션
* @Controller @ControllerAdvice 등을 사용할 수 있다.
* @Service @Component @Repository 등은 사용할수 없다.
* */
@WebMvcTest(controllers = HelloController.class, excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
public class HelloControllerTest {
    /*
    * Bean을 주입 받는다.
    * */
    @Autowired
    private MockMvc mvc;// 스프링 MVC테스트의 시작점

    @WithMockUser(roles = "USER")
    @Test
    public void hello가_리턴된다() throws Exception{
        String hello = "hello";

        mvc.perform(get("/hello"))// GET요청
                .andExpect(status().isOk())// 200인지 확인한다.
                .andExpect(content().string(hello));// 리턴 된것이 Hello인지 확인한다.
    }

    @WithMockUser(roles = "USER")
    @Test
    public void helloDto가_리턴된다() throws Exception{
        String name = "Hello";
        int amount = 1000;
        mvc.perform(
                get("/hello/dto")
                .param("name", name)
                .param("amount", Integer.toString(amount))
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(name))) // return 되는 JSON값을 필드별로 검증 할 수 있다.
                .andExpect(jsonPath("$.amount", is(amount)));
    }

}
