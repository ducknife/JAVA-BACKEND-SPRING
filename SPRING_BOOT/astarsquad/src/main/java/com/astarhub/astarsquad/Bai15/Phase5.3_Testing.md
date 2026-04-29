# 🧪 Phase 5.3: Testing — JUnit 5 + Mockito

---

## 📑 Mục Lục

- [1. Testing Là Gì? Tại Sao Cần?](#1-testing-là-gì-tại-sao-cần)
- [2. Các Loại Test](#2-các-loại-test)
- [3. Unit Test — JUnit 5 + Mockito](#3-unit-test--junit-5--mockito)
  - [3.1 JUnit 5 Annotations](#31-junit-5-annotations)
  - [3.2 Assertions](#32-assertions)
  - [3.3 Mockito — Mock Dependencies](#33-mockito--mock-dependencies)
  - [3.4 Ví Dụ: Test Service Layer](#34-ví-dụ-test-service-layer)
- [4. Integration Test](#4-integration-test)
  - [4.1 @SpringBootTest](#41-springboottest)
  - [4.2 @WebMvcTest — Test Controller](#42-webmvctest--test-controller)
  - [4.3 @DataJpaTest — Test Repository](#43-datajpatest--test-repository)
- [5. Test Security — @WithMockUser](#5-test-security--withmockuser)
- [6. Best Practices](#6-best-practices)
- [✅ Checklist](#-checklist)

---

## 1. Testing Là Gì? Tại Sao Cần?

```
Không test:
Code → Deploy → Bug phát hiện bởi USER → Fix → Deploy lại → Tốn $$$

Có test:
Code → Test FAIL → Fix → Test PASS → Deploy → Ít bug hơn
```

---

## 2. Các Loại Test

| Loại | Test gì | Tốc độ | Annotation |
|------|--------|--------|-----------|
| **Unit Test** | 1 class, mock dependencies | ⚡ Rất nhanh | `@ExtendWith(MockitoExtension.class)` |
| **Integration Test** | Nhiều class + DB thật | 🐌 Chậm | `@SpringBootTest` |
| **Slice Test** | 1 tầng (Controller/Repository) | 🔄 Vừa | `@WebMvcTest` / `@DataJpaTest` |

```
        ┌──────────────┐
        │  E2E Test    │  ← Ít nhất (chậm, đắt)
       ┌┴──────────────┴┐
       │ Integration    │
      ┌┴────────────────┴┐
      │   Unit Test      │  ← Nhiều nhất (nhanh, rẻ)
      └──────────────────┘
            Test Pyramid
```

---

## 3. Unit Test — JUnit 5 + Mockito

### 3.1 JUnit 5 Annotations

```java
@ExtendWith(MockitoExtension.class)  // Kích hoạt Mockito
class UserServiceTest {

    @BeforeEach   // Chạy TRƯỚC mỗi test method
    void setUp() { }

    @AfterEach    // Chạy SAU mỗi test method
    void tearDown() { }

    @Test         // Đánh dấu test method
    @DisplayName("Should create user successfully")
    void shouldCreateUser() { }

    @Test
    @Disabled("TODO: implement later")  // Bỏ qua test này
    void shouldDeleteUser() { }

    @ParameterizedTest   // Test với nhiều input
    @ValueSource(strings = {"", " ", "ab"})
    void shouldRejectInvalidUsername(String username) { }
}
```

### 3.2 Assertions

```java
import static org.assertj.core.api.Assertions.*;  // AssertJ (recommend)

@Test
void exampleAssertions() {
    // Cơ bản
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("admin");
    assertThat(result.getAge()).isGreaterThan(18);

    // Collection
    assertThat(users).hasSize(3);
    assertThat(users).extracting(User::getUsername)
        .containsExactly("alice", "bob", "charlie");

    // Exception
    assertThatThrownBy(() -> service.findById(999L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("User not found");

    // Not thrown
    assertThatCode(() -> service.findById(1L))
        .doesNotThrowAnyException();
}
```

### 3.3 Mockito — Mock Dependencies

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock                                // Tạo mock object
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks                         // Tạo real object, inject mocks vào
    private UserService userService;

    @Test
    void shouldFindUserById() {
        // GIVEN — setup mock behavior
        User user = User.builder().id(1L).username("admin").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // WHEN — gọi method cần test
        UserResponse result = userService.findById(1L);

        // THEN — verify kết quả
        assertThat(result.username()).isEqualTo("admin");
        verify(userRepository).findById(1L);  // Verify mock được gọi
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
            .isInstanceOf(NotFoundException.class);
    }
}
```

### 3.4 Ví Dụ: Test Service Layer

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private OrderService orderService;

    @Test
    @DisplayName("Create order — happy path")
    void shouldCreateOrder() {
        // Given
        User user = User.builder().id(1L).username("buyer").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(100L);
                return o;
            });

        CreateOrderRequest request = new CreateOrderRequest("Product A", 2);

        // When
        OrderResponse result = orderService.createOrder(request, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        verify(notificationService).sendOrderConfirmation(any());
    }
}
```

---

## 4. Integration Test

### 4.1 @SpringBootTest

```java
@SpringBootTest                    // Load toàn bộ Spring context
@AutoConfigureMockMvc             // Cấu hình MockMvc
@Transactional                     // Rollback DB sau mỗi test
class UserIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @Test
    void shouldCreateAndFetchUser() throws Exception {
        // Create
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"testuser","email":"test@mail.com","password":"Pass123!"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("testuser"));

        // Verify in DB
        assertThat(userRepository.findByUsername("testuser")).isPresent();
    }
}
```

### 4.2 @WebMvcTest — Test Controller

```java
@WebMvcTest(UserController.class)  // CHỈ load Controller + MVC config
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserService userService;  // Mock service

    @Test
    void shouldReturnUser() throws Exception {
        when(userService.findById(1L))
            .thenReturn(new UserResponse(1L, "admin", "admin@mail.com", List.of()));

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void shouldReturn400ForInvalidInput() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"","email":"invalid"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.username").exists());
    }
}
```

### 4.3 @DataJpaTest — Test Repository

```java
@DataJpaTest  // CHỈ load JPA config + embedded DB (H2)
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;
    @Autowired private TestEntityManager entityManager;

    @Test
    void shouldFindByUsername() {
        entityManager.persist(User.builder()
            .username("testuser").email("test@mail.com")
            .password("encoded").build());

        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@mail.com");
    }
}
```

---

## 5. Test Security — @WithMockUser

```java
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AdminService adminService;

    @Test
    @WithMockUser(roles = "ADMIN")  // Giả lập user có ROLE_ADMIN
    void adminCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")   // User thường
    void userCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedGetUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized());
    }
}
```

---

## 6. Best Practices

```
1. Tên test: should[Action]When[Condition]
   ✅ shouldThrowWhenUserNotFound
   ❌ test1, testMethod

2. Pattern: Given-When-Then (AAA: Arrange-Act-Assert)

3. Mỗi test chỉ test 1 behavior

4. Unit test KHÔNG nên gọi DB, API, file system

5. @Transactional trên integration test → auto rollback
```

---

## ✅ Checklist

- [ ] Unit Test: JUnit 5 + Mockito (@Mock, @InjectMocks)
- [ ] AssertJ assertions (fluent API)
- [ ] Given-When-Then pattern
- [ ] @WebMvcTest cho Controller
- [ ] @DataJpaTest cho Repository
- [ ] @SpringBootTest cho Integration
- [ ] @WithMockUser cho Security test

---

> **Tiếp theo**: Đọc `Phase5.4_API_Documentation.md` →
