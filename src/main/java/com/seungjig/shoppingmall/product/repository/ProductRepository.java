package com.seungjig.shoppingmall.product.repository;

import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);

    Page<Product> findByStockQuantityGreaterThanAndStatus(
            int stockQuantity,
            ProductStatus status,
            Pageable pageable
    );
}
