package com.portfolio.ecommerce.domain.user.dto;

import com.portfolio.ecommerce.domain.user.Address;
import com.portfolio.ecommerce.domain.user.User;
import com.portfolio.ecommerce.domain.user.UserRole;
import com.portfolio.ecommerce.domain.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private Address address;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getPhoneNumber(),
            user.getAddress(),
            user.getRole(),
            user.getStatus(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
