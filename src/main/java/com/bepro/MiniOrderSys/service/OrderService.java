package com.bepro.MiniOrderSys.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bepro.MiniOrderSys.dto.request.CreateOrderRequest;
import com.bepro.MiniOrderSys.dto.request.OrderItemRequest;
import com.bepro.MiniOrderSys.dto.request.UpdateOrderStatusRequest;
import com.bepro.MiniOrderSys.dto.response.OrderItemResponse;
import com.bepro.MiniOrderSys.dto.response.OrderResponse;
import com.bepro.MiniOrderSys.dto.response.ActiveTableResponse;
import com.bepro.MiniOrderSys.entity.CafeOrder;
import com.bepro.MiniOrderSys.entity.CafeTable;
import com.bepro.MiniOrderSys.entity.OrderItem;
import com.bepro.MiniOrderSys.entity.Product;
import com.bepro.MiniOrderSys.entity.enums.OrderStatus;
import com.bepro.MiniOrderSys.entity.enums.TableStatus;
import com.bepro.MiniOrderSys.repository.CafeOrderRepository;
import com.bepro.MiniOrderSys.repository.CafeTableRepository;
import com.bepro.MiniOrderSys.repository.ProductRepository;
import com.bepro.MiniOrderSys.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

  final CafeOrderRepository cafeOrderRepository;
  final CafeTableRepository cafeTableRepository;
  final ProductRepository productRepository;
  final UserRepository userRepository;

  private OrderResponse toResponse(CafeOrder order) {
    List<OrderItemResponse> itemResponses = order.getItems().stream()
        .map(item -> new OrderItemResponse(
            item.getProduct().getId(),
            item.getProductName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getLineTotal()))
        .toList();

    return new OrderResponse(
        order.getId(),
        order.getTableNumber(),
        order.getStatus().name(),
        order.getOrderedBy(),
        order.getTotalAmount(),
        order.getCreatedAt(),
        itemResponses);
  }

  @Transactional
  public OrderResponse createOrder(CreateOrderRequest request, String username) {
    String tableNumber = request.tableNumber().trim().toUpperCase();
    if (tableNumber.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Table number is required");
    }

    CafeTable table = cafeTableRepository.findByTableNumberIgnoreCase(tableNumber)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Table does not exist"));

    if (table.getStatus() == TableStatus.OUT_OF_SERVICE) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Table is currently out of service");
    }

    CafeOrder order = CafeOrder.builder()
        .tableNumber(tableNumber)
        .status(OrderStatus.ORDERED)
        .orderedBy("GUEST")
        .totalAmount(BigDecimal.ZERO)
        .build();

    BigDecimal totalAmount = BigDecimal.ZERO;

    for (OrderItemRequest itemRequest : request.items()) {
      Product product = productRepository.findById(itemRequest.productId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

      if (!product.getActive()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is not available for ordering");
      }

      BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
      totalAmount = totalAmount.add(lineTotal);

      OrderItem item = OrderItem.builder()
          .product(product)
          .productName(product.getName())
          .quantity(itemRequest.quantity())
          .unitPrice(product.getPrice())
          .lineTotal(lineTotal)
          .build();

      order.addItem(item);
    }

    if (username != null) {
      userRepository.findByUsername(username).ifPresent(user -> {
        order.setUser(user);
        order.setOrderedBy(user.getUsername());
      });
    }

    order.setTotalAmount(totalAmount);
    CafeOrder savedOrder = cafeOrderRepository.save(order);

    if (table.getStatus() == TableStatus.AVAILABLE) {
      table.setStatus(TableStatus.OCCUPIED);
      cafeTableRepository.save(table);
    }

    return toResponse(savedOrder);
  }

  @Transactional(readOnly = true)
  public List<OrderResponse> getAllOrders() {
    return cafeOrderRepository.findAllByOrderByCreatedAt().stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ActiveTableResponse> getActiveTables() {
    List<CafeOrder> activeOrders = cafeOrderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.ORDERED);

    Map<String, Long> groupedByTable = activeOrders.stream()
        .collect(Collectors.groupingBy(
            CafeOrder::getTableNumber,
            LinkedHashMap::new,
            Collectors.counting()));

    return groupedByTable.entrySet().stream()
        .map(entry -> new ActiveTableResponse(entry.getKey(), entry.getValue()))
        .toList();
  }

  @Transactional
  public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
    CafeOrder order = cafeOrderRepository.findById(orderId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

    order.setStatus(request.status());
    CafeOrder updatedOrder = cafeOrderRepository.save(order);

    syncTableStatusAfterOrderUpdate(updatedOrder.getTableNumber(), updatedOrder.getStatus());

    return toResponse(updatedOrder);
  }

  private void syncTableStatusAfterOrderUpdate(String tableNumber, OrderStatus status) {
    cafeTableRepository.findByTableNumberIgnoreCase(tableNumber).ifPresent(table -> {
      if (status == OrderStatus.ORDERED) {
        if (table.getStatus() == TableStatus.AVAILABLE) {
          table.setStatus(TableStatus.OCCUPIED);
          cafeTableRepository.save(table);
        }
        return;
      }

      long activeOrderCount = cafeOrderRepository.countByTableNumberAndStatus(tableNumber, OrderStatus.ORDERED);
      if (activeOrderCount == 0 && table.getStatus() == TableStatus.OCCUPIED) {
        table.setStatus(TableStatus.AVAILABLE);
        cafeTableRepository.save(table);
      }
    });
  }
}
