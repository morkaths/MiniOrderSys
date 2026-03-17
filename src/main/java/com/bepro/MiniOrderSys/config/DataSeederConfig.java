package com.bepro.MiniOrderSys.config;

import com.bepro.MiniOrderSys.entity.AppUser;
import com.bepro.MiniOrderSys.entity.enums.Role;
import com.bepro.MiniOrderSys.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeederConfig {

  @Bean
  public CommandLineRunner seedDefaultUsers(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    return args -> {
      createUserIfMissing(userRepository, passwordEncoder, "admin", "admin123", Role.ADMIN);
      createUserIfMissing(userRepository, passwordEncoder, "user", "user123", Role.USER);
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
}
