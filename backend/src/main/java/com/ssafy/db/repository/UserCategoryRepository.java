package com.ssafy.db.repository;

import com.ssafy.db.entity.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCategoryRepository extends JpaRepository<UserCategory, Long> {
    void deleteAllByUserId(Long userId);
}
