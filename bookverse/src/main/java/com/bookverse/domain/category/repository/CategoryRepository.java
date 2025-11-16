package com.bookverse.domain.category.repository;

import com.bookverse.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 카테고리 Repository
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 카테고리명으로 조회
     */
    Optional<Category> findByName(String name);

    /**
     * 카테고리명 중복 확인
     */
    boolean existsByName(String name);
}
