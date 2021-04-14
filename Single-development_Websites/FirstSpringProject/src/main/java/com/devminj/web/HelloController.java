package com.devminj.web;

import com.devminj.web.dto.HelloResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//JSON을 반환하는 컨트롤러로 만들어 준다.
//@ResponseBody를 각 메소드마다 선언했던것을 한번에 사용할 수 있게 해준다
@RestController
public class HelloController {
    //Get의 요청을 받을 수 있는 API
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    @GetMapping("/hello/dto")        //외부에서 API로 넘긴 파라미터를 가져오는 어노테이션이다.
    public HelloResponseDto helloDto(@RequestParam("name") String name, @RequestParam("amount") int amount){
        return new HelloResponseDto(name,amount);
    }
}
