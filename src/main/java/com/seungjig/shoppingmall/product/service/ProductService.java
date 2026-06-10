package com.seungjig.shoppingmall.product.service;

import com.seungjig.shoppingmall.global.util.FileService;
import com.seungjig.shoppingmall.product.entity.Product;
import com.seungjig.shoppingmall.product.entity.ProductStatus;
import com.seungjig.shoppingmall.product.entity.dto.AdminProductListDto;
import com.seungjig.shoppingmall.product.entity.dto.ProductCreateDto;
import com.seungjig.shoppingmall.product.entity.dto.ProductUpdateDto;
import com.seungjig.shoppingmall.product.exception.NotCorrectFileTypeException;
import com.seungjig.shoppingmall.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FileService fileService;

    // 상품 등록
    @Transactional
    public void createProduct(ProductCreateDto productCreateDto) throws IOException {

        Product product = new Product(productCreateDto.getName(), productCreateDto.getPrice(), productCreateDto.getStock(), ProductStatus.ACTIVE);

        if (fileService.isImageFile(productCreateDto.getImageHead())) {
            String imageHeadStore = fileService.storeFile(productCreateDto.getImageHead());
            product.setImageHeadUrl(imageHeadStore);
        } else {
            throw new NotCorrectFileTypeException("이미지 파일이 아닙니다.");
        }

        optionalImageProcess(productCreateDto.getImage1(), product::setImage1Url);
        optionalImageProcess(productCreateDto.getImage2(), product::setImage2Url);
        optionalImageProcess(productCreateDto.getImage3(), product::setImage3Url);

        productRepository.save(product);
    }

    // 관리자 상품 목록 리스트
    @Transactional(readOnly = true)
    public Page<AdminProductListDto> getAdminProductList(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToAdminProductListDto);
    }


    // 상품 수정
    @Transactional
    public void updateProduct(ProductUpdateDto productUpdateDto) throws IOException {

        Product product = productRepository.findById(productUpdateDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        product.setName(productUpdateDto.getName());
        product.setPrice(productUpdateDto.getPrice());
        product.setStock(productUpdateDto.getStock());
        product.setStatus(productUpdateDto.getStatus());

        if (productUpdateDto.getImageHead() != null && !productUpdateDto.getImageHead().isEmpty()) {
            // 저장소에 있는 기존 이미지 삭제
            fileService.deleteFile(product.getImageHeadUrl());
            // 새 이미지 저장하고 저장 파일명 수정
            String storeFile = fileService.storeFile(productUpdateDto.getImageHead());
            product.setImageHeadUrl(storeFile);
        }

        updateProductImage(productUpdateDto.getIsImage1Deleted(), productUpdateDto.getImage1(), product.getImage1Url(), product::setImage1Url);
        updateProductImage(productUpdateDto.getIsImage2Deleted(), productUpdateDto.getImage2(), product.getImage2Url(), product::setImage2Url);
        updateProductImage(productUpdateDto.getIsImage3Deleted(), productUpdateDto.getImage3(), product.getImage3Url(), product::setImage3Url);
    }

    private void updateProductImage(boolean isDeleted, MultipartFile newImageFile, String currentImageUrl, Consumer<String> imageSetter) throws IOException {

        if (isDeleted) {
            fileService.deleteFile(currentImageUrl);
            imageSetter.accept(null);
        } else if (fileService.isImageFile(newImageFile)) {
            fileService.deleteFile(currentImageUrl);
            String storeFileName = fileService.storeFile(newImageFile);
            imageSetter.accept(storeFileName);
        }
    }


    private AdminProductListDto mapToAdminProductListDto(Product product) {
        AdminProductListDto adminProductListDto = new AdminProductListDto();
        adminProductListDto.setId(product.getId());
        adminProductListDto.setName(product.getName());
        adminProductListDto.setPrice(product.getPrice());
        adminProductListDto.setStock(product.getStock());
        adminProductListDto.setStatus(product.getStatus());

        return adminProductListDto;
    }

    private void optionalImageProcess(MultipartFile multipartFile, Consumer<String> imageSetter) throws IOException {

        if (fileService.isImageFile(multipartFile)) {
            String urlStore = fileService.storeFile(multipartFile);
            imageSetter.accept(urlStore);
        }
    }
}
