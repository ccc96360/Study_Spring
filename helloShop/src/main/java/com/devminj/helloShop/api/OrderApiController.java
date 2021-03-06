package com.devminj.helloShop.api;

import com.devminj.helloShop.domain.Address;
import com.devminj.helloShop.domain.Order;
import com.devminj.helloShop.domain.OrderItem;
import com.devminj.helloShop.domain.OrderStatus;
import com.devminj.helloShop.repository.OrderRepository;
import com.devminj.helloShop.repository.OrderSearch;
import com.devminj.helloShop.repository.order.query.OrderFlatDto;
import com.devminj.helloShop.repository.order.query.OrderItemQueryDto;
import com.devminj.helloShop.repository.order.query.OrderQueryDto;
import com.devminj.helloShop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    private List<Order> ordersV1(){
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    private List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v3/orders")
    private List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v3.1/orders")
    private List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit){
        // xToOne ????????? ?????? Fetch Join ?????? ???????????? ???
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v4/orders")
    private List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    private List<OrderQueryDto> ordersV5(){
        return orderQueryRepository.findAllByDtoOptimization();
    }

    @GetMapping("/api/v6/orders")
    private List<OrderQueryDto> ordersV6(){
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDtoFlat();
        return flats.stream()
                .collect(
                        Collectors.groupingBy(
                                o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                                Collectors.mapping(o-> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), Collectors.toList())
                        )
                )
                .entrySet().stream()
                .map(e -> {
                    OrderQueryDto key = e.getKey();
                    List<OrderItemQueryDto> value = e.getValue();
                    return new OrderQueryDto(key.getOrderId(), key.getName(), key.getOrderDate(), key.getOrderStatus(), key.getAddress(), value);
                })
                .collect(Collectors.toList());

    }

    @Getter
    static class OrderDto{

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order){
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            this.orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto{

        private String itemName; // ?????? ???
        private int orderPrice; // ?????? ??????
        private int count; // ?????? ??????

        public OrderItemDto(OrderItem orderItem){
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
