package com.meteormin.friday.infrastructure.security;

import com.meteormin.friday.common.error.RestErrorCode;
import com.meteormin.friday.infrastructure.persistence.entities.UserEntity;
import com.meteormin.friday.infrastructure.persistence.repositories.UserEntityRepository;
import com.meteormin.friday.infrastructure.security.social.SocialProvider;
import com.meteormin.friday.infrastructure.security.social.userinfo.OAuth2Attributes;
import com.meteormin.friday.infrastructure.security.social.userinfo.OAuth2UserInfo;
import com.meteormin.friday.users.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * The type Principal user service.
 *
 * @author meteormin
 * @since 2023/08/27
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PrincipalUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    /**
     * The User repository.
     */
    private final UserEntityRepository userRepository;

    /**
     * Build principal user info principal user info.
     *
     * @param entity the entity
     * @param attributes the attributes
     * @return the principal user info
     * @author meteormin
     * @since 2023/08/27
     */
    private PrincipalUserInfo buildPrincipalUserInfo(UserEntity entity,
            Map<String, Object> attributes) {
        SocialProvider provider = entity.getProvider();

        return PrincipalUserInfo.builder()
                .id(entity.getId())
                .snsId(entity.getSnsId())
                .email(entity.getEmail())
                .name(entity.getName())
                .password(entity.getPassword())
                .enabled(entity.getDeletedAt() == null)
                .accountNonExpired(entity.getDeletedAt() == null)
                .accountNonLocked(entity.getDeletedAt() == null)
                .credentialsNonExpired(entity.getDeletedAt() == null)
                .attributes(attributes)
                .provider(provider)
                .authorities(getAuthorities(entity))
                .role(entity.getRole())
                .build();
    }

    /**
     * Gets authorities.
     *
     * @param entity the entity
     * @return the authorities
     * @author meteormin
     * @since 2023/08/27
     */
    private Collection<? extends GrantedAuthority> getAuthorities(UserEntity entity) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add((GrantedAuthority) () -> entity.getRole().value());

        return authorities;
    }

    /**
     * Create principal user info.
     *
     * @param userInfo the user info
     * @return the principal user info
     * @author meteormin
     * @since 2023/08/27
     */
    public PrincipalUserInfo create(OAuth2UserInfo userInfo) {
        UserEntity entity = UserEntity.builder()
                .snsId(userInfo.snsId())
                .provider(userInfo.getProvider())
                .email(userInfo.email())
                .password(userInfo.getProvider() + ":" + userInfo.snsId())
                .name(userInfo.name())
                .role(UserRole.USER)
                .build();
        entity = userRepository.save(entity);

        return buildPrincipalUserInfo(entity, userInfo.attributes());
    }

    /**
     * Loads the OAuth2User for the given OAuth2UserRequest.
     *
     * @param userRequest the OAuth2UserRequest object containing the request details
     * @return the OAuth2User representing the loaded user
     * @throws OAuth2AuthenticationException if an error occurs during the authentication process
     * @author meteormin
     * @since 2023/08/27
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        PrincipalUserInfo user;

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);


        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuth2Attributes oAuthAttributes =
                OAuth2Attributes.of(registrationId, userNameAttributeName,
                        oAuth2User.getAttributes());

        OAuth2UserInfo userInfo = oAuthAttributes.toUserInfo();
        Optional<UserEntity> userEntity = userRepository
                .findBySnsIdAndProvider(userInfo.snsId(), userInfo.getProvider());

        if (userEntity.isPresent()) {
            user = buildPrincipalUserInfo(
                    userEntity.get(),
                    oAuthAttributes.getAttributes());
        } else {
            user = create(userInfo);
        }

        return user;
    }

}
