package com.seungjig.shoppingmall.product;

import com.seungjig.shoppingmall.global.security.CustomUserDetails;
import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.entity.ProductStatus;
import com.seungjig.shoppingmall.product.repository.ProductRepository;
import com.seungjig.shoppingmall.user.entity.Role;
import com.seungjig.shoppingmall.user.entity.User;
import com.seungjig.shoppingmall.user.entity.UserStatus;
import com.seungjig.shoppingmall.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("상품 등록 성공")
    void createProductSuccess() throws Exception {

        //given
        User saved = userRepository.save(
                new User("loginId", "password", "admin", "010-1234-5678", Role.ADMIN, UserStatus.ACTIVE));

        CustomUserDetails customUserDetails = new CustomUserDetails(saved);

        MockMultipartFile imageHead = new MockMultipartFile(
                "imageHead",
                "imageHead.png",
                MediaType.IMAGE_PNG_VALUE,
                "image_data_bytes".getBytes()
        );

        String uniqueName = UUID.randomUUID().toString();

        //when
        mockMvc.perform(multipart("/products/create")
                        .with(csrf())
                        .with(user(customUserDetails))
                        .file(imageHead)
                .param("name", uniqueName)
                .param("price", "10000")
                .param("stock", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        //then
        Product product = productRepository.findByName(uniqueName)
                .orElseThrow(() -> new AssertionError("상품이 저장되지 않았습니다."));

        assertThat(product.getName()).isEqualTo(uniqueName);
        assertThat(product.getPrice()).isEqualTo(10000);
        assertThat(product.getStock()).isEqualTo(10);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getImageHeadUrl()).isNotBlank();

    }

    @Test
    @DisplayName("상품 등록 실패 - 관리자 권한 없음")
    void createProductFailure() throws Exception {

        //given
        User saved = userRepository.save(
                new User("loginId", "password", "user", "010-1234-5678", Role.USER, UserStatus.ACTIVE));

        CustomUserDetails customUserDetails = new CustomUserDetails(saved);

        MockMultipartFile imageHead = new MockMultipartFile(
                "imageHead",
                "imageHead.png",
                MediaType.IMAGE_PNG_VALUE,
                "image_data_bytes".getBytes()
        );

        String uniqueName = UUID.randomUUID().toString();

        //when
        mockMvc.perform(multipart("/products/create")
                        .with(csrf())
                        .with(user(customUserDetails))
                        .file(imageHead)
                        .param("name", uniqueName)
                        .param("price", "10000")
                        .param("stock", "10"))
                .andExpect(status().isForbidden());

        //then
        assertThat(productRepository.findByName(uniqueName)).isEmpty();
    }

}
