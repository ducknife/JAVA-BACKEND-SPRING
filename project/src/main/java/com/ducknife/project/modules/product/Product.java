package com.ducknife.project.modules.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
// @Data // với entity không nên dùng @Data, vì khi fix bug nó sẽ in theo dây chuyền nếu có khóa ngoại;
// ví dụ: in ra Order nó sẽ cố in ra User -> kích hoạt truy vấn tìm user 
@Getter
@Setter
@NoArgsConstructor // tạo constructor không tham số, để JPA không lỗi 
@AllArgsConstructor // tạo constructor có tất cả tham số, để @Builder không lỗi 
@Builder // tạo ra 1 object mà không quan tâm thứ tự thuộc tính, nhưng cần constructor đầy đủ thuộc tính 
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // dùng cho MySQL, là auto increment trong mysql 
    private Long id;

    @Column(name = "name", nullable = false, length = 200, unique = true)
    private String name;
    
    @Column(name = "price", scale = 2)
    private Double price;

    private Long category_id;
}
