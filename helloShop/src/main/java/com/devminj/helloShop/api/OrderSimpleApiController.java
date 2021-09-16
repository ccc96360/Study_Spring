package com.devminj.helloShop.api;

import com.devminj.helloShop.domain.Address;
import com.devminj.helloShop.domain.Order;
import com.devminj.helloShop.domain.OrderStatus;
import com.devminj.helloShop.repository.OrderRepository;
import com.devminj.helloShop.repository.OrderSearch;
import com.devminj.helloShop.repository.order.simplequery.OrderSimpleQueryDto;
import com.devminj.helloShop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for(Order order : all){
            order.getMember().getName(); // => Lazy 강제 초기화
            order.getDelivery().getAddress();
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        // N + 1문제 발생
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        return orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        // N + 1문제 발생
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.orderId =  order.getId();
            this.name =  order.getMember().getName();
            this.orderDate =  order.getOrderDate();
            this.orderStatus =  order.getStatus();
            this.address = order.getDelivery().getAddress();
        }
    }
}
