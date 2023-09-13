package com.miniyus.friday.infrastructure.security.oauth2.userinfo;

import java.util.Map;
import com.miniyus.friday.infrastructure.security.oauth2.OAuth2Provider;

/**
 * [description]
 *
 * @author miniyus
 * @date 2023/08/31
 */
public interface OAuth2UserInfo {
    String snsId();

    OAuth2Provider getProvider();

    String email();

    String name();

    Map<String, Object> attributes();
}
