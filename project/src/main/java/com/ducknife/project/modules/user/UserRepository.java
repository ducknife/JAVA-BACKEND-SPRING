package com.ducknife.project.modules.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
        Boolean existsByUserName(String userName);

        List<User> findTop2ByOrderByUserNameDesc();

        List<User> findTop2ByOrderByFullNameDesc();

        List<User> findByIdBetween(Long left, Long right);

        List<User> findByFullNameLike(String subName);

        List<User> findByFullNameLikeAndUserNameLike(String fullName, String userName);

        @Query("SELECT u FROM User u WHERE LENGTH(u.userName) <= :length ORDER BY u.fullName DESC")
        List<User> findByUserNameLengthOrderByFullNameDesc(@Param("length") Long length); // Param giúp nối một biến vào
                                                                                          // đúng vị trí câu lệnh trên
                                                                                          // Query.

        @Query("SELECT u FROM User u ORDER BY LENGTH(u.fullName) DESC LIMIT 1")
        List<User> findTop1ByOrderByFullNameLengthDesc();

        @Query("SELECT u FROM User u ORDER BY LENGTH(u.userName) DESC")
        Page<User> findByNameLength(Pageable pageable);

        @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE CONCAT('%', LOWER(:keyword), '%')")
        List<User> findByFullname(@Param("keyword") String keyword);

        @Lock(LockModeType.PESSIMISTIC_WRITE) // khóa bi quan, khóa vật lý dòng, sinh ra for update, lock cứng db, service phải có transactional 
        @QueryHints({ @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") }) // giới hạn 3 giây, nếu ko lấy đc khóa, ném lỗi
        List<User> findByIdLessThan(Long id, Sort sort);
}
