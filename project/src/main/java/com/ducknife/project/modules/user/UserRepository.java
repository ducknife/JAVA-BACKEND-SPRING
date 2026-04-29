package com.ducknife.project.modules.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
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

        @EntityGraph(attributePaths = { "roles", "roles.permissions" })
        Optional<User> findByUsername(String username);

        Boolean existsByUsername(String username);

        Boolean existsByRolesName(String roleName);

        List<User> findTop2ByOrderByUsernameDesc();

        List<User> findTop2ByOrderByFullnameDesc();

        List<User> findByIdBetween(Long left, Long right);

        List<User> findByFullnameLike(String subname);

        List<User> findByFullnameLikeAndUsernameLike(String fullname, String username);

        @Query("SELECT u FROM User u WHERE LENGTH(u.username) <= :length ORDER BY u.fullname DESC")
        List<User> findByUsernameLengthOrderByFullnameDesc(@Param("length") Long length); // Param giúp nối một biến vào
                                                                                          // đúng vị trí câu lệnh trên
                                                                                          // Query.

        @Query("SELECT u FROM User u ORDER BY LENGTH(u.fullname) DESC LIMIT 1")
        List<User> findTop1ByOrderByFullnameLengthDesc();

        @Query("SELECT u FROM User u ORDER BY LENGTH(u.username) DESC")
        Page<User> findByNameLength(Pageable pageable);

        @Query("SELECT u FROM User u WHERE LOWER(u.fullname) LIKE CONCAT('%', LOWER(:keyword), '%')")
        List<User> findByFullname(@Param("keyword") String keyword);

        @Lock(LockModeType.PESSIMISTIC_WRITE) // khóa bi quan, khóa vật lý dòng, sinh ra for update, lock cứng db,
                                              // service phải có transactional
        @QueryHints({ @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000") }) // giới hạn 3 giây, nếu
                                                                                               // ko lấy đc khóa, ném
                                                                                               // lỗi
        @EntityGraph(attributePaths = "roles")
        List<User> findByIdLessThan(Long id, Sort sort);
}
