package com.seungjig.shoppingmall.order.service;

import com.seungjig.shoppingmall.order.entity.Order;
import com.seungjig.shoppingmall.order.entity.OrderItem;
import com.seungjig.shoppingmall.order.entity.OrderStatus;
import com.seungjig.shoppingmall.order.entity.dto.OrderCreateDto;
import com.seungjig.shoppingmall.order.entity.dto.OrderItemCreateDto;
import com.seungjig.shoppingmall.order.exception.OutOfStockException;
import com.seungjig.shoppingmall.order.repository.OrderItemRepository;
import com.seungjig.shoppingmall.order.repository.OrderRepository;
import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.entity.ProductStatus;
import com.seungjig.shoppingmall.product.repository.ProductRepository;
import com.seungjig.shoppingmall.user.entity.Role;
import com.seungjig.shoppingmall.user.entity.User;
import com.seungjig.shoppingmall.user.entity.UserStatus;
import com.seungjig.shoppingmall.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class OrderServiceIntegrationTest {

    @Autowired
    OrderService orderService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("주문이 생성되면 주문 상품의 재고가 주문 수량만큼 차감된다")
    void should_decreaseStock_when_orderCreated() {
        // given
        User user = new User("loginId", "hashedPassword", "userName", "010-1234-5678", Role.USER, UserStatus.ACTIVE);
        userRepository.save(user);

        Product product = new Product("productName", 10000, 100, ProductStatus.ACTIVE);
        product.setImageHeadUrl("imageHeadUrl");
        productRepository.save(product);

        OrderItemCreateDto orderItemCreateDto = new OrderItemCreateDto();
        orderItemCreateDto.setProductId(product.getId());
        orderItemCreateDto.setQuantity(2);
        orderItemCreateDto.setUnitPrice(product.getPrice());
        orderItemCreateDto.setImageHeadUrl("imageHeadUrl");
        orderItemCreateDto.setProductName(product.getName());

        OrderCreateDto orderCreateDto = new OrderCreateDto();
        orderCreateDto.setTotalPrice(orderItemCreateDto.getUnitPrice() * orderItemCreateDto.getQuantity());
        orderCreateDto.setAddress("address");
        orderCreateDto.setAddressDetail("addressDetail");
        orderCreateDto.setPostalCode("01234");
        orderCreateDto.setOrdererName(user.getName());
        orderCreateDto.setOrdererPhoneNumber(user.getPhoneNumber());
        orderCreateDto.setRecipientName("recipientName");
        orderCreateDto.setRecipientPhoneNumber("010-0000-1111");
        orderCreateDto.setOrderList(List.of(orderItemCreateDto));
        orderCreateDto.setIsCartOrder(false);

        int originalStock = product.getStock();

        // when
        orderService.createOrder(orderCreateDto, user, orderCreateDto.getIsCartOrder());
        em.flush();
        em.clear();

        // then
        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new AssertionError("상품이 존재하지 않습니다. ID: " + product.getId()));
        assertThat(updatedProduct.getStock()).isEqualTo(originalStock - orderItemCreateDto.getQuantity());
    }

    @Test
    @DisplayName("상품의 재고보다 많은 주문 요청이 왔을 시 OutOfStockException 발생")
    void should_outOfStockException() {
        //given
        User user = new User("loginId", "hashedPassword", "userName", "010-1234-5678", Role.USER, UserStatus.ACTIVE);
        userRepository.save(user);

        Product product = new Product("productName", 10000, 100, ProductStatus.ACTIVE);
        product.setImageHeadUrl("imageHeadUrl");
        productRepository.save(product);

        OrderItemCreateDto orderItemCreateDto = new OrderItemCreateDto();
        orderItemCreateDto.setProductId(product.getId());
        orderItemCreateDto.setQuantity(200);
        orderItemCreateDto.setUnitPrice(product.getPrice());
        orderItemCreateDto.setImageHeadUrl("imageHeadUrl");
        orderItemCreateDto.setProductName(product.getName());

        OrderCreateDto orderCreateDto = new OrderCreateDto();
        orderCreateDto.setTotalPrice(orderItemCreateDto.getUnitPrice() * orderItemCreateDto.getQuantity());
        orderCreateDto.setAddress("address");
        orderCreateDto.setAddressDetail("addressDetail");
        orderCreateDto.setPostalCode("01234");
        orderCreateDto.setOrdererName(user.getName());
        orderCreateDto.setOrdererPhoneNumber(user.getPhoneNumber());
        orderCreateDto.setRecipientName("recipientName");
        orderCreateDto.setRecipientPhoneNumber("010-0000-1111");
        orderCreateDto.setOrderList(List.of(orderItemCreateDto));
        orderCreateDto.setIsCartOrder(false);

        //when&then
        assertThatThrownBy(() -> orderService.createOrder(orderCreateDto, user, orderCreateDto.getIsCartOrder()))
                .isInstanceOf(OutOfStockException.class);
        em.clear();

        Product stockCheckProduct =
                productRepository.findById(product.getId()).orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));
        assertThat(stockCheckProduct.getStock()).isEqualTo(100);
    }

    @Test
    @DisplayName("주문 목록을 조회할 때 쿼리가 4번 나가야 한다.")
    void should_4query() {
        //given
        User user = new User("loginId", "hashedPassword", "userName", "010-1234-5678", Role.USER, UserStatus.ACTIVE);
        userRepository.save(user);

        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            Product product = new Product("productName" + i, 1000 * i, i, ProductStatus.ACTIVE);
            product.setImageHeadUrl("imageHeadUrl" + i);
            products.add(product);
        }
        productRepository.saveAll(products);

        List<Order> orders = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();
        int productsIndex = 0;
        int totalPriceIndex = 0;
        for (int i = 1; i <= 3; i++) {
            Order order = Order.builder()
                    .user(user)
                    .totalPrice(products.subList(totalPriceIndex, totalPriceIndex+3).stream().mapToInt(Product::getPrice).sum())
                    .orderStatus(OrderStatus.ORDERED)
                    .address("address")
                    .addressDetail("addressDetail")
                    .postalCode("01234")
                    .ordererName(user.getName())
                    .ordererPhoneNumber("010-1234-1234")
                    .recipientName("recipientName")
                    .recipientPhoneNumber("010-1234-5678")
                    .build();
            orders.add(order);
            totalPriceIndex += 3;

            for (int j = 1; j <= 3; j++) {
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(products.get(productsIndex))
                        .quantity(1)
                        .unitPrice(products.get(productsIndex).getPrice())
                        .productName(products.get(productsIndex).getName())
                        .build();
                orderItems.add(orderItem);
                productsIndex++;
            }
        }

        orderRepository.saveAll(orders);
        orderItemRepository.saveAll(orderItems);

        em.flush();
        em.clear();

        SessionFactory sessionFactory = em.getEntityManagerFactory()
                .unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();


        //when
        orderService.getOrderListDto(user, PageRequest.of(0, 3, Sort.by("createdAt").descending()));

        //then
        long queryCount = statistics.getPrepareStatementCount();
        assertThat(queryCount).isEqualTo(4);
    }
}
