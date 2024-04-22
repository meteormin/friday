package com.miniyus.friday.unit.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.miniyus.friday.infrastructure.jpa.entities.UserEntity;
import com.miniyus.friday.infrastructure.jpa.repositories.UserRepository;
import com.miniyus.friday.infrastructure.security.PrincipalUserInfo;
import com.miniyus.friday.infrastructure.security.PrincipalUserService;
import com.miniyus.friday.infrastructure.security.oauth2.userinfo.GoogleUserInfo;
import com.miniyus.friday.infrastructure.security.oauth2.userinfo.OAuth2UserInfo;

/**
 * [description]
 *
 * @author seongminyoo
 * @date 2023/09/06
 */
@ExtendWith(MockitoExtension.class)
public class PrincipalUserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PrincipalUserService principalUserService;

    private UserEntity testUser;

    /**
     * Set up the test environment before each test case.
     *
     */
    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .snsId("test1234")
                .email("miniyus@gmail.com")
                .name("smyoo")
                .provider("google")
                .build();
    }

    /**
     * Creates an OAuth2 user.
     *
     */
    @Test
    void createOAuth2User() {
        when(userRepository.save(any())).thenReturn(testUser);
        
        OAuth2UserInfo userInfo = GoogleUserInfo.builder()
                .snsId("test1234")
                .email("miniyu97@gmail.com")
                .name("smyoo")
                .attributes(null)
                .build();

        PrincipalUserInfo principal = principalUserService.create(userInfo);
        assertNotNull(principal, "failed to create principal");
        assertNotEquals(testUser.getProvider(), userInfo.snsId(), "not equal snsId");
        assertNotEquals(testUser.getEmail(), userInfo.email(), "not equal email");
        assertNotEquals(testUser.getProvider(), userInfo.getProvider(), "not equal provider");
    }
}
