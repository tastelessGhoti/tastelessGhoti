package com.bookverse.domain.category.entity;

import com.bookverse.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 도서 카테고리 엔티티
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(length = 200)
    private String description;

    /**
     * 카테고리 정보 수정
     */
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
