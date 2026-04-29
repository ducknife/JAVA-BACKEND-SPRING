package com.ducknife.project.modules.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ducknife.project.modules.order.Order;
import com.ducknife.project.modules.role.Role;
import com.ducknife.project.modules.user.dto.UserRequest;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
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

    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;

    @Column(name = "username", unique = true, nullable = false, length = 100)
    private String username;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    // mappedBy = "user" -> Trỏ vào biến 'private User user' trong class Order
    // Nếu thiếu mappedBy -> JPA sẽ tự tạo ra bảng trung gian (User_Order) -> SAI
    // thiết kế
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    @ManyToMany
    @JoinTable(
        name = "user_roles", 
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Version // khóa lạc quan: nếu version đã
             // được update (có transaction khác sửa và lưu trước mình), thì nó báo lỗi
             // OptimisticLockException
    private Long version;

    public static User from(UserRequest user) {
        return User.builder()
                .fullname(user.getFullname())
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }

}
// Inverse side: Luôn là phía @OneToMany, chỉ mang ý nghĩa read-only;
// Bắt buộc dùng mappedBy trỏ vào tên biến bên Owning side;

// CascadeType.PERSIST: Lưu cha -> lưu con (nếu có cập nhật);
// CascadeType.REMOVE: Xóa cha -> xóa tất cả các con;
// CascadeType.ALL: Bao gồm tất cả quyền trên;
// Nếu dùng chay jdbcTemplate phải thêm on delete cascade để xóa khóa ngoại liên
// quan;
