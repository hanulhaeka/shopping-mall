package com.seungjig.shoppingmall.product.service;

import com.seungjig.shoppingmall.global.util.FileStore;
import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.entity.ProductStatus;
import com.seungjig.shoppingmall.product.entity.dto.ProductCreateDto;
import com.seungjig.shoppingmall.product.exception.NotCorrectFileTypeException;
import com.seungjig.shoppingmall.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStore fileStore;

    // 상품 등록
    @Transactional
    public void createProduct(ProductCreateDto productCreateDto) throws IOException {

        Product product = new Product(productCreateDto.getName(), productCreateDto.getPrice(), productCreateDto.getStock(), ProductStatus.ACTIVE);

        if (fileStore.isImageFile(productCreateDto.getImageHead())) {
            String imageHeadStore = fileStore.storeFile(productCreateDto.getImageHead());
            product.setImageHeadUrl(imageHeadStore);
        } else {
            throw new NotCorrectFileTypeException("이미지 파일이 아닙니다.");
        }

        optionalImageProcess(productCreateDto.getImage1(), product::setImage1Url);
        optionalImageProcess(productCreateDto.getImage2(), product::setImage2Url);
        optionalImageProcess(productCreateDto.getImage3(), product::setImage3Url);

        productRepository.save(product);
    }

    private void optionalImageProcess(MultipartFile multipartFile, Consumer<String> imageSetter) throws IOException {

        if (fileStore.isImageFile(multipartFile)) {
            String urlStore = fileStore.storeFile(multipartFile);
            imageSetter.accept(urlStore);
        }
    }
}
