package com.devminj.basic.singleton;

public class StatefulService {

    //stateful
//    private int price;

    public int order(String name, int price){
        System.out.println("name = " + name + " price = " + price);
        return price;
    }

//    public int getPrice(){
//        return price;
//    }
}
