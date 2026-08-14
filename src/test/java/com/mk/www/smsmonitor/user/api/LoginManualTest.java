package com.mk.www.smsmonitor.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mk.www.smsmonitor.user.api.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Disabled("수동 로그인 테스트")
public class LoginManualTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void mkw_로그인_테스트_비밀번호_11() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLoginId("mkw"); // 아이디 수정
        loginRequest.setPassword("11"); // 비밀번호

        System.out.println(">>> [테스트] mkw / PW: 11 로 로그인 시도 중...");
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();

        System.out.println(">>> [결과] mkw 로그인 성공!");
        System.out.println(">>> [응답본문] " + result.getResponse().getContentAsString());
    }
}
