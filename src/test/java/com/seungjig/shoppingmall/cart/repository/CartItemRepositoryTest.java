package com.seungjig.shoppingmall.cart.repository;

import com.seungjig.shoppingmall.cart.entity.Cart;
import com.seungjig.shoppingmall.cart.entity.CartItem;
import com.seungjig.shoppingmall.global.config.JpaConfig;
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

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(JpaConfig.class)
public class CartItemRepositoryTest {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("장바구니 조회시 N+1 문제 방지를 위해 fetch join 확인")
    void getCartWithCartList() {
        //given
        User user = new User("loginId", "password", "name", "010-1234-5678", Role.USER, UserStatus.ACTIVE);
        Cart cart = new Cart(user);
        Product product1 = new Product("product1", 10000, 100, ProductStatus.ACTIVE);
        product1.setImageHeadUrl("imageHeadUrl1");
        CartItem cartItem1 = new CartItem(cart, product1, 10, product1.getPrice());
        Product product2 = new Product("product2", 20000, 200, ProductStatus.ACTIVE);
        product2.setImageHeadUrl("imageHeadUrl2");
        CartItem cartItem2 = new CartItem(cart, product2, 20, product2.getPrice());

        productRepository.save(product1);
        productRepository.save(product2);

        userRepository.save(user);

        cartRepository.save(cart);

        cartItemRepository.save(cartItem1);
        cartItemRepository.save(cartItem2);

        em.flush();
        em.clear();

        //when
        List<CartItem> cartItems = cartItemRepository.findByCartIdWithProduct(cart.getId());

        //then
        assertThat(cartItems).hasSize(2);

        PersistenceUnitUtil unitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        assertThat(unitUtil.isLoaded(cartItems.get(0).getProduct())).isTrue();
        assertThat(unitUtil.isLoaded(cartItems.get(1).getProduct())).isTrue();

    }

}
