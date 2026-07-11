package com.seungjig.shoppingmall.product.service;

import com.seungjig.shoppingmall.global.util.FileService;
import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.entity.ProductStatus;
import com.seungjig.shoppingmall.product.entity.dto.ProductUpdateDto;
import com.seungjig.shoppingmall.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
public class ProductServiceIntegrationTest {

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    EntityManager em;

    @MockitoBean
    FileService fileService;

    @Test
    @DisplayName("상품 수정 성공")
    void productUpdateSuccess() throws IOException {
        //given
        Product product = new Product("name", 10000, 50, ProductStatus.ACTIVE);
        product.setImageHeadUrl("imageHeadUrl.png");
        Product saved = productRepository.save(product);

        ProductUpdateDto updateDto = new ProductUpdateDto();
        updateDto.setId(saved.getId());
        updateDto.setName("newName");
        updateDto.setPrice(20000);
        updateDto.setStock(100);
        updateDto.setStatus(ProductStatus.INACTIVE);
        updateDto.setImageHead(new MockMultipartFile(
                "imageHead",
                "newHeadImage.png",
                MediaType.IMAGE_PNG_VALUE,
                "image_data_bytes".getBytes()));

        given(fileService.storeFile(updateDto.getImageHead())).willReturn("savedImageFileName.png");

        //when
        productService.updateProduct(updateDto);

        em.flush();
        em.clear();

        //then
        Product updateProduct = productRepository.findById(saved.getId()).get();
        assertThat(updateProduct.getName()).isEqualTo("newName");
        assertThat(updateProduct.getPrice()).isEqualTo(20000);
        assertThat(updateProduct.getStock()).isEqualTo(100);
        assertThat(updateProduct.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        assertThat(updateProduct.getImageHeadUrl()).isEqualTo("savedImageFileName.png");
        verify(fileService).deleteFile(anyString());
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void productDeleteSuccess() {
        //given
        Product product = new Product("name", 10000, 50, ProductStatus.ACTIVE);
        product.setImageHeadUrl("imageHeadUrl.png");
        Product saved = productRepository.save(product);

        //when
        productService.softDeleteProduct(saved.getId());
        em.flush();
        em.clear();

        //then
        Product softDeleteProduct = productRepository.findById(saved.getId()).get();
        assertThat(softDeleteProduct.getStatus()).isEqualTo(ProductStatus.DELETE);
    }

}
