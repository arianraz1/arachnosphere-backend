package com.tarantulacommunity.tarantulasocial.security.mapper;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.dto.request.LoginRequest;
import com.tarantulacommunity.tarantulasocial.security.dto.response.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthMapperTest {

    @Autowired
    private AuthMapper authMapper;

    @Test
    void testToLoginResponse() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        
        LoginResponse response = authMapper.toLoginResponse(user);
        
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void testToLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        
        LoginRequest result = authMapper.toLoginRequest(request);
        
        assertNotNull(result);
        assertSame(request, result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("password", result.getPassword());
    }
}
