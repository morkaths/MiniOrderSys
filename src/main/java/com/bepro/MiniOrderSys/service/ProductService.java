package com.bepro.MiniOrderSys.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bepro.MiniOrderSys.dto.request.ProductRequest;
import com.bepro.MiniOrderSys.dto.response.ProductRespone;
import com.bepro.MiniOrderSys.entity.Product;
import com.bepro.MiniOrderSys.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  private String safeTrim(String s) {
    return s == null ? "" : s.trim();
  }

  public ProductRespone toRespone(Product product) {
    return new ProductRespone(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getPrice().toString(),
        product.getActive());
  }

  public Product toEntity(ProductRequest p) {
    return Product.builder()
        .name(safeTrim(p.name()))
        .description(safeTrim(p.description()))
        .price(p.price())
        .active(p.active())
        .build();
  }

  @Transactional(readOnly = true)
  public List<ProductRespone> getAllProducts() {
    return productRepository.findAll().stream()
        .map(this::toRespone)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductRespone> getAvailableProducts() {
    return productRepository.findByActiveTrueOrderByNameAsc().stream()
        .map(this::toRespone)
        .toList();
  }

  @Transactional(readOnly = true)
  public ProductRespone getProductById(Long id) {
    return productRepository.findById(id)
        .filter(Product::getActive)
        .map(this::toRespone)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Product not found with id: " + id));
  }

  @Transactional
  public ProductRespone createProduct(ProductRequest request) {
    Product product = toEntity(request);
    return toRespone(productRepository.save(product));
  }

  @Transactional
  public ProductRespone updateProduct(Long id, ProductRequest request) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

    product.setName(safeTrim(request.name()));
    product.setDescription(safeTrim(request.description()));
    product.setPrice(request.price());

    if (request.active() != null) {
      product.setActive(request.active() != null && request.active());
    }

    return toRespone(productRepository.save(product));
  }

  @Transactional
  public void deleteProduct(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    productRepository.delete(product);
  }
}
