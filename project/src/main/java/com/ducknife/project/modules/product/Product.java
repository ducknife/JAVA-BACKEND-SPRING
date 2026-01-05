package com.ducknife.project.modules.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor // tạo constructor không tham số, để JPA không lỗi 
@AllArgsConstructor // tạo constructor có tất cả tham số, để @Builder không lỗi 
@Builder // tạo ra 1 object mà không quan tâm thứ tự thuộc tính, nhưng cần constructor đầy đủ thuộc tính 
public class Product {
    @Id
    private Long id;
    private String name;
    private double price;
    private Long category_id;
}
