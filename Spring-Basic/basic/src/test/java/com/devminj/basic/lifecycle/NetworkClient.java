package com.devminj.basic.lifecycle;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class NetworkClient /*implements InitializingBean, DisposableBean*/ {

    private String url;
/*
    @Override
    public void afterPropertiesSet() throws Exception {
        connect();
        call("초기화 연결 메시지");
    }

    @Override
    public void destroy() throws Exception {
        disconnect();
    }
*/
/*
    public void init() throws Exception {
        connect();
        call("초기화 연결 메시지");
    }

    public void close() throws Exception {
        disconnect();
    }
*/
    @PostConstruct
    public void init() throws Exception {
        connect();
        call("초기화 연결 메시지");
    }

    @PreDestroy
    public void close() throws Exception {
        disconnect();
    }


    public NetworkClient() {
        System.out.println("생성자 호출, url = " + url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //서비스 사작시 호출
    public void connect(){
        System.out.println("connect: " + url);
    }

    public void call(String msg){
        System.out.println("call: " + url + " message = " + msg);
    }

    //서비스 종료시 호출
    public void disconnect(){
        System.out.println("close " + url);
    }
}
