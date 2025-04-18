package com.cordestitch.controller.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cordestitch.filter.IdEncryptor;
import com.cordestitch.response.token.AuthTokenResponse;
import com.cordestitch.response.token.SessionStatusResponse;
import com.cordestitch.service.serviceimplementation.token.JwtServiceImplementation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(JwtController.class)
class JwtControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private JwtServiceImplementation jwtService;

    @MockBean
    private HttpServletResponse response;

    @MockBean
    private HttpServletRequest request;

    @MockBean
    private IdEncryptor idEncryptor;

    @MockBean
    private AuthTokenResponse authTokenResponse;

    @Autowired
    private ObjectMapper objectMapper;
    private static final String ENCRYPTED_USER_ID = "dGsWBYqcp6YJfgrJEgShJ49SzBm7Uv6FTR9aKPvdJOGeuUC7Ow";
    private static final String DECRYPTED_USER_ID = "user1";


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        when(idEncryptor.decrypt(ENCRYPTED_USER_ID))
                .thenReturn(DECRYPTED_USER_ID);    }

    @Test
    void getAccessToken_ShouldReturnAccessToken() throws Exception {
        String userId = ENCRYPTED_USER_ID;
        String mockToken = "mockAccessToken123";
        when(jwtService.generateToken(userId, "ROLE_USER")).thenReturn(mockToken);

        mockMvc.perform(get("/api/jwt/getAccessToken")
                        .param("userId", userId)
                        .param("userType", "ROLE_USER")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void refreshToken_ShouldReturnAccessToken() throws Exception {
        when(jwtService.refreshToken(request,response)).thenReturn(authTokenResponse);

        mockMvc.perform(get("/api/jwt/refreshToken")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void sessionStatus() throws Exception{
        when(jwtService.validateSession(request,response)).thenReturn(new SessionStatusResponse());
        mockMvc.perform(get("/api/jwt/sessionStatus")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}