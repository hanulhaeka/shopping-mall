package com.seungjig.shoppingmall.order.repository;

import com.seungjig.shoppingmall.global.config.JpaConfig;
import com.seungjig.shoppingmall.order.entity.Order;
import com.seungjig.shoppingmall.order.entity.OrderItem;
import com.seungjig.shoppingmall.order.entity.OrderStatus;
import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.entity.ProductStatus;
import com.seungjig.shoppingmall.product.repository.ProductRepository;
import com.seungjig.shoppingmall.user.entity.Role;
import com.seungjig.shoppingmall.user.entity.User;
import com.seungjig.shoppingmall.user.entity.UserStatus;
import com.seungjig.shoppingmall.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(JpaConfig.class)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("Order 엔티티 조회 시 OrderItem과 Product 엔티티가 fetch join되서 즉시 로딩된다.")
    void should_fetchJoin_with_OrderItem_and_Product() {
        // given
        User user = new User("loginId", "hashedPassword", "userName", "010-1234-5678", Role.USER, UserStatus.ACTIVE);
        userRepository.save(user);

        Product product = new Product("productName", 10000, 100, ProductStatus.ACTIVE);
        product.setImageHeadUrl("imageHeadUrl");
        productRepository.save(product);

        Order order = Order.builder()
                .user(user)
                .totalPrice(product.getPrice())
                .orderStatus(OrderStatus.ORDERED)
                .address("address")
                .addressDetail("addressDetail")
                .postalCode("01234")
                .ordererName("ordererName")
                .recipientName("recipientName")
                .build();
        orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .unitPrice(product.getPrice())
                .productName(product.getName())
                .build();
        orderItemRepository.save(orderItem);

        //when
        em.flush();
        em.clear();

        Order fetchJoinOrder = orderRepository.findOrderWithItemsAndProducts(order.getId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문이거나 올바른 접근이 아닙니다."));

        // then
        PersistenceUnitUtil unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(unitUtil.isLoaded(fetchJoinOrder, "orderItems")).isTrue();
        OrderItem fetchJoinOrderItem = fetchJoinOrder.getOrderItems().getFirst();
        assertThat(unitUtil.isLoaded(fetchJoinOrderItem.getProduct())).isTrue();

    }
}
