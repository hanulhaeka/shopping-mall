package com.seungjig.shoppingmall.order.service;

import com.seungjig.shoppingmall.cart.entity.Cart;
import com.seungjig.shoppingmall.cart.repository.CartItemRepository;
import com.seungjig.shoppingmall.cart.repository.CartRepository;
import com.seungjig.shoppingmall.global.util.FileService;
import com.seungjig.shoppingmall.order.entity.Order;
import com.seungjig.shoppingmall.order.entity.OrderItem;
import com.seungjig.shoppingmall.order.entity.dto.OrderCreateDto;
import com.seungjig.shoppingmall.order.entity.dto.OrderItemCreateDto;
import com.seungjig.shoppingmall.order.exception.IncorrectPriceException;
import com.seungjig.shoppingmall.order.exception.OutOfStockException;
import com.seungjig.shoppingmall.order.repository.OrderItemRepository;
import com.seungjig.shoppingmall.order.repository.OrderRepository;
import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.repository.ProductRepository;
import com.seungjig.shoppingmall.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FileService fileService;

    // 주문 생성
    @Transactional
    public Long createOrder(OrderCreateDto orderCreateDto, User user, boolean isCartOrder) {
        Order order = Order.builder()
                .user(user)
                .totalPrice(orderCreateDto.getTotalPrice())
                .address(orderCreateDto.getAddress())
                .addressDetail(orderCreateDto.getAddressDetail())
                .postalCode(orderCreateDto.getPostalCode())
                .ordererName(orderCreateDto.getOrdererName())
                .ordererPhoneNumber(orderCreateDto.getOrdererPhoneNumber())
                .recipientName(orderCreateDto.getRecipientName())
                .recipientPhoneNumber(orderCreateDto.getRecipientPhoneNumber())
                .build();

        orderRepository.save(order);

        List<OrderItemCreateDto> orderItemDtoList = orderCreateDto.getOrderList();
        List<Long> productIds = orderItemDtoList.stream().map(OrderItemCreateDto::getProductId).toList();
        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        if (productMap.size() != productIds.size()) throw new IllegalArgumentException("존재하지 않는 상품이 포함되어 있습니다.");

        List<OrderItem> orderItemsToSave = new ArrayList<>();
        Integer totalPriceCheck = 0;

        for (OrderItemCreateDto dto : orderItemDtoList) {

            Product product = productMap.get(dto.getProductId());

            if (!Objects.equals(product.getPrice(), dto.getUnitPrice())) throw new IncorrectPriceException("데이터베이스 상품 가격과 일치하지 않습니다.");
            totalPriceCheck += product.getPrice() * dto.getQuantity();

            // 재고 차감
            int result = productRepository.decreaseStock(product.getId(), dto.getQuantity());
            if (result == 0) throw new OutOfStockException("재고가 부족하거나 존재하지 않는 상품입니다.");

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(dto.getQuantity())
                    .unitPrice(product.getPrice())
                    .productName(product.getName())
                    .build();

            orderItemsToSave.add(orderItem);
        }

        if (!Objects.equals(totalPriceCheck, orderCreateDto.getTotalPrice())) {
            throw new IncorrectPriceException("요청된 총 금액과 계산된 총 금액이 다릅니다.");
        }

        orderItemRepository.saveAll(orderItemsToSave);

        if (isCartOrder) {
            Cart cart = cartRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원이거나 존재하지 않는 장바구니 입니다."));
            cartItemRepository.deleteAllByCartId(cart.getId());
        }


        return order.getId();
    }

    @Transactional(readOnly = true)
    public OrderCreateDto prepareDirectOrder(Long productId, Integer quantity, User user) {
        int stock = getStock(productId);
        if (stock < quantity) {
            throw new OutOfStockException("재고가 부족합니다.");
        }

        List<OrderItemCreateDto> orderItemCreateDtoList = getOrderItemCreateDtoList(List.of(productId), List.of(quantity));
        OrderCreateDto orderCreateDto = new OrderCreateDto();
        orderCreateDto.setOrderList(orderItemCreateDtoList);
        orderCreateDto.setOrdererName(user.getName());
        orderCreateDto.setOrdererPhoneNumber(user.getPhoneNumber());

        return orderCreateDto;
    }

    // 재고 확인
    public int getStock(Long productId) {
        return productRepository.findStockById(productId);
    }

    // OrderItemCreateDto 리스트 반환
    public List<OrderItemCreateDto> getOrderItemCreateDtoList(List<Long> productIds, List<Integer> quantities) {
        if (productIds == null || quantities == null || productIds.size() != quantities.size()) {
            throw new IllegalArgumentException("상품 목록과 수량 목록의 개수가 일치하지 않습니다.");
        }

        List<OrderItemCreateDto> list = new ArrayList<>();
        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        Set<Long> uniqueProductIds = new HashSet<>(productIds);
        if (productMap.size() != uniqueProductIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 상품이 포함되어 있습니다.");
        }

        for (int i = 0; i < productIds.size(); i++) {
            long productId = productIds.get(i);
            Product product = productMap.get(productId);
            OrderItemCreateDto orderItemCreateDto = new OrderItemCreateDto();
            orderItemCreateDto.setProductId(productId);
            orderItemCreateDto.setQuantity(quantities.get(i));
            orderItemCreateDto.setUnitPrice(product.getPrice());
            orderItemCreateDto.setImageHeadUrl(fileService.getFullPath(product.getImageHeadUrl()));
            list.add(orderItemCreateDto);
        }

        return list;

    }

}
