package com.ducknife.project.modules.user;

import java.util.List;

import com.ducknife.project.modules.order.Order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "user_name", unique = true, nullable = false, length = 100)
    private String userName;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    // mappedBy = "user" -> Trỏ vào biến 'private User user' trong class Order
    // Nếu thiếu mappedBy -> JPA sẽ tự tạo ra bảng trung gian (User_Order) -> SAI thiết kế
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
}
// Inverse side: Luôn là phía @OneToMany, chỉ mang ý nghĩa read-only;
// Bắt buộc dùng mappedBy trỏ vào tên biến bên Owning side;

// CascadeType.PERSIST: Lưu cha -> lưu con (nếu có cập nhật);
// CascadeType.REMOVE: Xóa cha -> xóa tất cả các con;
// CascadeType.ALL: Bao gồm tất cả quyền trên;
// Nếu dùng chay jdbcTemplate phải thêm on delete cascade để xóa khóa ngoại liên quan;
