package com.ducknife.project.modules.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
        Boolean existsByUserName(String userName);
        List<User> findTop2ByOrderByUserNameDesc();
        List<User> findTop2ByOrderByFullNameDesc();
        List<User> findByIdBetween(Long left, Long right);
        List<User> findByFullNameLike(String subName);
        List<User> findByFullNameLikeAndUserNameLike(String fullName, String userName);

        @Query("SELECT u FROM User u WHERE LENGTH(u.userName) <= ?1 ORDER BY u.fullName DESC")
        List<User> findByUserNameLengthOrderByFullNameDesc(Long length);
        @Query("SELECT u FROM User u ORDER BY LENGTH(u.fullName) DESC LIMIT 1")
        List<User> findTop1ByOrderByFullNameLengthDesc();

}
