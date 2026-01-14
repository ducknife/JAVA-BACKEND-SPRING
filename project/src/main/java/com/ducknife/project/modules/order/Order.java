package com.ducknife.project.modules.order;

import com.ducknife.project.modules.user.User;

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
@Table(name = "sale_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

// Owning side: Giữ khóa ngoại, luôn là @ManyToOne, Chịu trách nhiệm lưu/sửa mối quan hệ
// Dùng @JoinColumn để chỉ định cột khóa ngoại 

// FetchType: 
// 1. EAGER: tải toàn bộ dữ liệu liên quan ngay lập tức , default của ManyToOne, OneToOne 
// 2. LAZY: chỉ tải dữ liệu khi thấy lệnh .get...(), default của OneToMany, ManyToMany 
// Luôn ưu tiên LAZY, còn sau này sẽ dùng JOIN FETCH để tối ưu 
