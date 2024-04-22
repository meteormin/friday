package com.miniyus.friday.unit.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.miniyus.friday.infrastructure.jpa.entities.UserEntity;
import com.miniyus.friday.infrastructure.jpa.repositories.UserRepository;
import com.miniyus.friday.infrastructure.security.PrincipalUserDetailsService;
import com.miniyus.friday.infrastructure.security.PrincipalUserInfo;
import com.miniyus.friday.infrastructure.security.auth.userinfo.PasswordUserInfo;
import jakarta.validation.Validator;

/**
 * A test for the PrincipalUserDetailsService class.
 *
 * @author seongminyoo
 * @date 2023/09/06
 */
@ExtendWith(MockitoExtension.class)
public class PrincipalUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private Validator validator;

    @InjectMocks
    private PrincipalUserDetailsService principalUserDetailsService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .name("miniyus")
                .password("test@password")
                .email("miniyu97@gmail.com")
                .build();
    }

    /**
     * A test for the createPasswordUserInfoTest() method.
     */
    @Test
    void createPasswordUserInfoTest() {
        PasswordUserInfo userInfo = PasswordUserInfo.builder()
                .email(testUser.getEmail())
                .password(testUser.getPassword())
                .name(testUser.getName())
                .build();

        var validated = validator.validate(userInfo);
        assertTrue(validated.isEmpty(), "failed to validate passwordUserInfo");

        when(userRepository.save(any())).thenReturn(testUser);
        principalUserDetailsService.setPasswordEncoder(passwordEncoder);
        PrincipalUserInfo principalUserInfo = principalUserDetailsService.create(userInfo);

        assertEquals(testUser.getEmail(), principalUserInfo.getUsername(), "not equal email");
        assertEquals(testUser.getName(), principalUserInfo.getName(), "not equal name");
    }

    @Test
    void loadUserByUsernameTest() {
        String testEmail = testUser.getEmail();

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        PrincipalUserInfo principalUserInfo = (PrincipalUserInfo) principalUserDetailsService
                .loadUserByUsername(testEmail);

        assertEquals(testUser.getEmail(), principalUserInfo.getUsername(), "not equal email");
        assertEquals(testUser.getName(), principalUserInfo.getName(), "not equal name");
    }
}
