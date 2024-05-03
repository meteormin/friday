package com.meteormin.friday.infrastructure.persistence.repositories;

import com.meteormin.friday.infrastructure.persistence.entities.UserEntity;
import com.meteormin.friday.infrastructure.security.social.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User Repository
 *
 * @author meteormin
 * @since 2023/09/02
 */
public interface UserEntityRepository extends JpaRepository<UserEntity, Long>,
        QUserEntityRepository {
    /**
     * Finds a user entity by email.
     *
     * @param email the email of the user to find
     * @return an optional containing the found user entity, or empty if not found
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Finds a user entity by social media ID and provider.
     *
     * @param snsId the social media ID of the user
     * @param provider the provider of the social media platform
     * @return an optional user entity that matches the given social media ID and provider
     */
    Optional<UserEntity> findBySnsIdAndProvider(String snsId, SocialProvider provider);

    /**
     * Checks if an email exists.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    boolean existsByEmail(String email);

}
