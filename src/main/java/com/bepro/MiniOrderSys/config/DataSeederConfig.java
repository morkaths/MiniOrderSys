package com.bepro.MiniOrderSys.config;

import com.bepro.MiniOrderSys.entity.AppUser;
import com.bepro.MiniOrderSys.entity.Product;
import com.bepro.MiniOrderSys.entity.enums.Role;
import com.bepro.MiniOrderSys.repository.ProductRepository;
import com.bepro.MiniOrderSys.repository.UserRepository;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeederConfig {

  @Bean
  public CommandLineRunner seedDefaultUsers(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      ProductRepository productRepository) {
    return args -> {
      createUserIfMissing(userRepository, passwordEncoder, "admin", "admin123", Role.ADMIN);
      createUserIfMissing(userRepository, passwordEncoder, "user", "user123", Role.USER);

      seedProductsIfEmpty(productRepository);
    };
  }

  private void createUserIfMissing(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      String username,
      String rawPassword,
      Role role) {
    userRepository.findByUsername(username).orElseGet(() -> {
      AppUser user = AppUser.builder()
          .username(username)
          .password(passwordEncoder.encode(rawPassword))
          .role(role)
          .build();

      return userRepository.save(user);
    });
  }

  private void seedProductsIfEmpty(ProductRepository productRepository) {
    if (productRepository.count() > 0) {
      return;
    }

    productRepository.save(Product.builder()
        .name("Ca phe den")
        .description("Ca phe den da")
        .price(new BigDecimal("25000"))
        .active(true)
        .build());

    productRepository.save(Product.builder()
        .name("Ca phe sua")
        .description("Ca phe sua da")
        .price(new BigDecimal("30000"))
        .active(true)
        .build());

    productRepository.save(Product.builder()
        .name("Tra dao cam sa")
        .description("Tra dao tuoi mat")
        .price(new BigDecimal("35000"))
        .active(true)
        .build());

    productRepository.save(Product.builder()
        .name("Bac xiu")
        .description("Sua nhieu ca phe it")
        .price(new BigDecimal("32000"))
        .active(true)
        .build());
  }
}
