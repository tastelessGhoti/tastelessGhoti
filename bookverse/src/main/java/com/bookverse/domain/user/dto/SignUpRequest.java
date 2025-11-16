package com.bookverse.domain.user.dto;

import com.bookverse.domain.user.entity.Address;
import com.bookverse.domain.user.entity.User;
import com.bookverse.domain.user.entity.UserRole;
import com.bookverse.domain.user.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 *
 * @author Ghoti
 * @since 2025-11-17
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
    private String name;

    @Pattern(regexp = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$",
            message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;

    private String zipCode;
    private String address;
    private String detailAddress;

    /**
     * DTO를 Entity로 변환
     */
    public User toEntity(String encodedPassword) {
        Address addressEntity = null;
        if (this.address != null) {
            addressEntity = Address.builder()
                    .zipCode(this.zipCode)
                    .address(this.address)
                    .detailAddress(this.detailAddress)
                    .build();
        }

        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .address(addressEntity)
                .role(UserRole.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
