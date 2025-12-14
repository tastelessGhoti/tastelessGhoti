package com.kakaopay.account.integration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaopay.account.domain.member.dto.SignUpRequest;
import com.kakaopay.account.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("회원 API 통합 테스트")
class MemberApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/members - 회원가입 성공")
    void 회원가입_API_성공() throws Exception {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .ci("TESTCI1234567890123456789012345678901234567890123456789012345678901234567890123456")
                .name("김테스트")
                .phoneNumber("01011112222")
                .email("test@kakaopay.com")
                .birthDate("19950315")
                .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.member_id").exists())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/v1/members - 필수값 누락 시 400 에러")
    void 회원가입_필수값_누락() throws Exception {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .name("김테스트")
                .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("POST /api/v1/members - 잘못된 전화번호 형식")
    void 회원가입_전화번호_형식_오류() throws Exception {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .ci("TESTCI1234567890123456789012345678901234567890123456789012345678901234567890123456")
                .name("김테스트")
                .phoneNumber("02-1234-5678")
                .build();

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/v1/members/{id} - 존재하지 않는 회원 조회 시 404 에러")
    void 존재하지_않는_회원_조회() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/members/99999")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("M001"));
    }
}
