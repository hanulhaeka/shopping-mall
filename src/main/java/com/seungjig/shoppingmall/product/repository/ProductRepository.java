package com.seungjig.shoppingmall.product.repository;

import com.seungjig.shoppingmall.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
