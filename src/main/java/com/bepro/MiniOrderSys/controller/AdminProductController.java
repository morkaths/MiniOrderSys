package com.bepro.MiniOrderSys.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bepro.MiniOrderSys.dto.request.ProductRequest;
import com.bepro.MiniOrderSys.dto.response.ProductRespone;
import com.bepro.MiniOrderSys.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

  private final ProductService productService;

  @GetMapping
  public List<ProductRespone> getAllProducts() {
    return productService.getAllProducts();
  }

  @PostMapping
  public ResponseEntity<ProductRespone> createProduct(@Valid @RequestBody ProductRequest request) {
    ProductRespone response = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductRespone> updateProduct(@PathVariable Long id,
      @Valid @RequestBody ProductRequest request) {
    ProductRespone response = productService.updateProduct(id, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }
}
