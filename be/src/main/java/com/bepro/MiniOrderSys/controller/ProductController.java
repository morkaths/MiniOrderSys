package com.bepro.MiniOrderSys.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bepro.MiniOrderSys.dto.response.ProductRespone;
import com.bepro.MiniOrderSys.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public List<ProductRespone> getAvailableProducts() {
    return productService.getAvailableProducts();
  }

  @GetMapping("/{id}")
  public ProductRespone getProductById(@PathVariable Long id) {
    return productService.getProductById(id);
  }
}

// public class ProductController {

// private final ProductService productService;

// public ProductController() {
// this.productService = new ProductService();
// }

// public List<ProductRespone> getAvailableProducts() {
// return productService.getAvailableProducts();
// }

// public ProductRespone getProductById(Long id) {
// return productService.getProductById(id);
// }
// }

// public ProductController(ProductService productService) {
// this.productService = productService;
// }
