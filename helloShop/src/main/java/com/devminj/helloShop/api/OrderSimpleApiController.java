package com.devminj.helloShop.api;

import com.devminj.helloShop.domain.Order;
import com.devminj.helloShop.repository.OrderRepository;
import com.devminj.helloShop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Order
 * Order -> Member 연관
 * Order -> Delivery 연관
 * xToOne 에서의 성능 최적화
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for(Order order : all){
            order.getMember().getName(); // => Lazy 강제 초기화
            order.getDelivery().getAddress();
        }
        return all;
    }
}
