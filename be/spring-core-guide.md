# Script giới thiệu Spring trong MiniOrderSys BE

Tài liệu này giải thích 4 khái niệm theo thứ tự:

1. IoC
2. DI
3. ApplicationContext
4. Bean

Nội dung được trích và diễn giải dựa trên code backend của dự án MiniOrderSys.

## 1) IoC (Inversion of Control)

### Định nghĩa ngắn gọn

IoC là nguyên lý đảo ngược quyền kiểm soát việc tạo và quản lý đối tượng.

- Không dùng Spring: class của bạn tự `new` dependency và tự quyết định vòng đời.
- Dùng Spring: container của Spring tạo object, gắn dependency, quản lý vòng đời và cấp cho class cần dùng.

### IoC trong dự án này

Điểm khởi động của ứng dụng là `MiniOrderSysApplication`:

```java
@SpringBootApplication
public class MiniOrderSysApplication {
  public static void main(String[] args) {
    SpringApplication.run(MiniOrderSysApplication.class, args);
  }
}
```

Annotation `@SpringBootApplication` chứa:

- `@SpringBootConfiguration`: là phiên bản đặc biệt của `@Configuration`, đánh dấu class là nguồn cấu hình chính
- `@EnableAutoConfiguration`: bật cơ chế auto-config của Spring Boot, ví dụ các gói `spring-boot-starter-web`, `spring-boot-starter-data-jpa`
- `@ComponentScan`: tự động scan các component trong package, scan từ package chứa class main, scan các annotation `@Component`, `@Service`, `@Repository`, `@Controller`

Khi `SpringApplication.run(...)` được gọi, Spring Boot tạo và khởi tạo IoC container. Từ đó:

- Quét package `com.bepro.MiniOrderSys`.
- Tìm các class có annotation như `@Service`, `@Component`, `@Configuration`, `@RestController`.
- Tạo object (bean), nối dependency, và đưa vào container.

### Ví dụ cụ thể trong code

- `InvoiceService` được đánh dấu `@Service`.
- `JwtAuthenticationFilter` được đánh dấu `@Component`.
- `AdminInvoiceController` được đánh dấu `@RestController`.

Bản thân các class này không tự tạo instance của mình. Spring container làm việc đó.

### Giá trị thực tế

IoC giúp:

- Giảm kết dính giữa các class.
- Dễ mở rộng và test hơn.
- Có một chỗ tập trung quản lý object toàn hệ thống.

---

## 2) DI (Dependency Injection)

### Định nghĩa ngắn gọn

DI là cơ chế cụ thể để hiện thực IoC: dependency được "inject" vào class thay vì class tự tạo.

### Kiểu DI trong dự án này

Dự án đang dùng **constructor injection** thông qua `final` + Lombok `@RequiredArgsConstructor`.

Ví dụ `AdminInvoiceController`:

```java
@RestController
@RequestMapping("/api/admin/invoices")
@RequiredArgsConstructor
public class AdminInvoiceController {
  private final InvoiceService invoiceService;
}
```

Controller không `new InvoiceService()`. Spring tìm bean `InvoiceService` và truyền vào constructor.

Ví dụ `InvoiceService` inject nhiều dependency:

```java
@Service
@RequiredArgsConstructor
public class InvoiceService {
  private final InvoiceRepository invoiceRepository;
  private final CafeOrderRepository cafeOrderRepository;
  private final CafeTableRepository cafeTableRepository;
  private final VNPayService vnPayService;
  private final SimpMessagingTemplate messagingTemplate;
}
```

### DI với interface

Trong `SecurityConfig`:

```java
private final UserDetailsService userDetailsService;
```

Dependency là interface. Lúc runtime, Spring sẽ tìm implementation phù hợp (trong dự án là `AppUserDetailsService`) để inject.

### DI với config object

`JwtService` và `VNPayService` inject `AppProperties`:

```java
private final AppProperties appProperties;
```

`AppProperties` đọc giá trị từ `application.yaml` (prefix `app`) rồi cấp cho service dùng.

### Lợi ích của DI ở đây

- `InvoiceService` tập trung business, không cần quan tâm tạo repository hay messaging template.
- Có thể thay implementation mà ít ảnh hưởng code (qua interface).
- Dễ mock dependency khi viết unit test.

---

## 3) ApplicationContext

### Định nghĩa ngắn gọn

`ApplicationContext` là IoC container của Spring ở mức cao:

- Chứa danh sách bean.
- Quản lý vòng đời bean.
- Xử lý wiring dependency.
- Hỗ trợ environment, properties, event, AOP, ...

### ApplicationContext xuất hiện như thế nào trong dự án

Trong `main`, dự án gọi:

```java
SpringApplication.run(MiniOrderSysApplication.class, args);
```

Lệnh này tạo và khởi tạo một `ApplicationContext` (thực tế là implementation của `ConfigurableApplicationContext`).

Dự án không cần gọi trực tiếp `applicationContext.getBean(...)` vì đang dùng một pattern tốt hơn:

- Khai báo annotation (`@Service`, `@Bean`, ...).
- Để Spring auto-wire theo constructor.

### Luồng khởi tạo (để dễ trình bày)

1. App start tại `main`.
2. Spring tạo `ApplicationContext`.
3. Context quét class trong package app.
4. Tạo bean từ annotation stereotype và `@Bean` methods.
5. Giải dependency graph và inject vào constructor.
6. Ứng dụng sẵn sàng nhận request.

### Ví dụ liên kết với project

- `SecurityConfig` cần `JwtAuthenticationFilter` và `UserDetailsService`.
- `InvoiceService` cần repository + service + messaging bean.
- Tất cả được context map và inject theo type.

---

## 4) Bean

### Định nghĩa ngắn gọn

Bean là object được Spring quản lý bên trong `ApplicationContext`.

### Các cách tạo bean trong dự án

#### 4.1 Bean từ stereotype annotation

- `@Service`: `InvoiceService`, `JwtService`, `VNPayService`, ...
- `@Component`: `JwtAuthenticationFilter`.
- `@RestController`: các controller API.
- `@Configuration`: class config như `SecurityConfig`, `DataSeederConfig`, `AppProperties`.

Spring scan và tạo bean tự động.

#### 4.2 Bean từ method có `@Bean`

Trong `SecurityConfig`:

- `SecurityFilterChain`
- `AuthenticationProvider`
- `PasswordEncoder`
- `AuthenticationManager`

Trong `DataSeederConfig`:

- `CommandLineRunner seedDefaultUsers(...)`

Đặc biệt, `seedDefaultUsers` là bean chạy khi app startup để seed user, product, table, voucher.

#### 4.3 Bean từ Spring Data JPA

Các interface repository như `UserRepository`, `InvoiceRepository` (extends `JpaRepository`) được Spring Data tạo implementation và đăng ký thành bean runtime.

### Scope và vòng đời

Mặc định trong project này, bean là singleton:

- Mỗi type thường chỉ có 1 instance trong context.
- Controller/Service chia sẻ instance đó trong suốt vòng đời app.

### Lifecycle của Bean

Lifecycle (vòng đời) của một bean trong Spring thường đi theo các bước:

1. Spring tạo instance bean (constructor).
2. Spring inject dependency vào bean.
3. Nếu có callback khởi tạo thì chạy (ví dụ `@PostConstruct`, `InitializingBean`, hoặc `initMethod`).
4. Bean sẵn sàng phục vụ request trong suốt thời gian ứng dụng chạy.
5. Khi `ApplicationContext` đóng, callback hủy được gọi (ví dụ `@PreDestroy`, `DisposableBean`, hoặc `destroyMethod`).

Trong dự án MiniOrderSys BE hiện tại:

- Các bean như `InvoiceService`, `OrderService`, `VNPayService`, `JwtAuthenticationFilter` đang dùng constructor injection, nên bước tạo instance + inject dependency diễn ra tự động bởi Spring.
- Chưa khai báo custom lifecycle callback kiểu `@PostConstruct`/`@PreDestroy` trong các class chính, nên vòng đời đang theo luồng mặc định của Spring.
- Bean `CommandLineRunner seedDefaultUsers(...)` trong `DataSeederConfig` là một ví dụ đặc biệt: logic bên trong sẽ được chạy ở giai đoạn startup sau khi context khởi tạo xong.

### Ví dụ kết nối bean thực tế

- Bean `PasswordEncoder` được dùng trong `DataSeederConfig` để encode mật khẩu.
- Bean `AuthenticationProvider` sử dụng `UserDetailsService` + `PasswordEncoder`.
- Bean `VNPayService` được inject vào `InvoiceService` để tạo payment URL.

---

## Tổng kết để trình bày nhanh

- IoC: Spring container nắm quyền tạo/quản lý object thay cho class.
- DI: container inject dependency qua constructor (`@RequiredArgsConstructor`).
- ApplicationContext: "bộ não" lưu và điều phối toàn bộ bean + wiring.
- Bean: từng object được context quản lý, tạo từ annotation, `@Bean`, hoặc Spring Data.

Nếu cần thuyết trình 3-5 phút, bạn có thể đi theo flow:

1. `main` khởi động app -> context được tạo.
2. Context scan tạo bean.
3. Context inject dependency vào controller/service.
4. Request vào API sẽ sử dụng các bean đã được wiring sẵn.

---

## Bổ sung: Optional, Lambda, Method Reference

### Optional trong dự án dùng để làm gì?

Trong MiniOrderSys BE, `Optional` được dùng chủ yếu để xử lý kết quả truy vấn có thể rỗng từ repository, tránh `NullPointerException` và làm flow nghiệp vụ rõ ràng hơn.

Một số chỗ điển hình:

- `OrderService#createOrder`: `productRepository.findById(...).orElseThrow(...)` để báo lỗi ngay khi không tìm thấy sản phẩm.
- `VoucherService#getMyVouchers`: `userRepo.findByUsername(...).orElseThrow(...)` để chặn case user không tồn tại.
- `DataSeederConfig#createUserIfMissing`: `findByUsername(...).orElseGet(...)` để chỉ tạo user khi chưa có.
- `VNPayController#createPaymentUrl`: `invoiceRepository.findById(...).orElseThrow(...)` để trả lỗi 404 khi invoice không tồn tại.

Ý nghĩa thực tế:

- Biến trạng thái “có hoặc không có dữ liệu” thành API rõ ràng.
- Tập trung xử lý lỗi tại điểm truy vấn, giảm kiểm tra `null` thủ công ở nhiều nơi.
- Code ngắn hơn, dễ đọc hơn cho các luồng nghiệp vụ.

### `map`, `filter` của Optional khác gì của Collection?

`Optional<T>` đại diện cho **tối đa 1 phần tử**.

- `Optional.map(f)`: nếu có giá trị thì biến đổi 1 giá trị đó, nếu rỗng thì vẫn rỗng.
- `Optional.filter(p)`: nếu có giá trị và thỏa điều kiện thì giữ lại, ngược lại thành rỗng.

`Collection<T>`/`Stream<T>` đại diện cho **0..n phần tử**.

- `Stream.map(f)`: biến đổi từng phần tử trong tập.
- `Stream.filter(p)`: lọc tập theo điều kiện.

Tóm lại: cùng tên hàm, nhưng ngữ nghĩa khác ở số lượng phần tử xử lý.

- `Optional`: xử lý “có 1 hay không có”.
- `Collection/Stream`: xử lý “nhiều phần tử”.

Ví dụ so sánh nhanh:

```java
Optional<String> name = userRepo.findByUsername(username).map(AppUser::getUsername);
List<String> names = users.stream().map(AppUser::getUsername).toList();
```

### Lambda trong dự án

Lambda là cách viết ngắn gọn cho functional interface.

Trong dự án có nhiều lambda rõ ràng:

- `orElseThrow(() -> new ResponseStatusException(...))`
- `.map(item -> new OrderItemResponse(...))`
- `ifPresent(user -> { order.setUser(user); ... })`
- Trong `VoucherService`: `Predicate`, `Supplier`, `Function`, `Consumer` đều dùng lambda.

Ưu điểm khi dùng lambda trong BE hiện tại:

- Viết logic ngắn gọn, giảm boilerplate class ẩn danh.
- Kết hợp tốt với `Optional` và `Stream` để viết pipeline xử lý dữ liệu.

### Method Reference trong dự án

Method reference là cú pháp rút gọn của lambda khi lambda chỉ gọi 1 method có sẵn.

Ví dụ đã có trong dự án:

```java
.map(this::toUserVoucherResponse)
.map(this::toResponse)
```

So với lambda tương đương:

```java
.map(uv -> toUserVoucherResponse(uv))
.map(order -> toResponse(order))
```

Khi nào ưu tiên method reference:

- Khi biểu thức lambda chỉ gọi 1 hàm có sẵn và không thêm xử lý phụ.
- Giúp code gọn, dễ quét nhanh trong các đoạn stream.

Khi nào giữ lambda:

- Khi cần thêm điều kiện, nhiều bước xử lý, hoặc đặt tên biến trung gian cho dễ hiểu.
