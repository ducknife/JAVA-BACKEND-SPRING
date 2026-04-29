package com.ducknife.project.modules.product;

import java.math.BigDecimal;

import com.ducknife.project.modules.category.Category;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
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
    
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY) // nếu dùng ở cả 2 phía cho quan hệ OneToMany thì phải dùng DTO để tránh vòng lặp vô hạn;
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // nếu dùng 1 phía như product - category trong dự án này thì phải thêm dòng này ở Owning Side để tránh lỗi do LAZY;
    private Category category;

    // @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<OrderDetail> orderDetails;
}
