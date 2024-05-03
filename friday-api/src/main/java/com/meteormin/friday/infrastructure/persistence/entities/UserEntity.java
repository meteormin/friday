package com.meteormin.friday.infrastructure.persistence.entities;

import com.meteormin.friday.common.fake.annotation.NoFake;
import com.meteormin.friday.infrastructure.persistence.BaseEntity;
import com.meteormin.friday.infrastructure.security.social.SocialProvider;
import com.meteormin.friday.users.domain.UserRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity
 *
 * @author meteormin
 * @since 2023/09/02
 */
@Entity
@Getter
@Setter
@SuperBuilder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auth_user")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE auth_user SET deleted_at = NOW() WHERE id = ?")
public class UserEntity extends BaseEntity<Long> {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String snsId;

    @Column
    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SocialProvider provider;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @NonNull
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private UserRole role;

    @Column
    private LocalDateTime deletedAt;

    @NoFake
    @Builder.Default
    @OrderBy("createdAt DESC")
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LoginHistoryEntity> loginHistories = new ArrayList<>();

    @NoFake
    @Builder.Default
    @OrderBy("createdAt DESC")
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HostEntity> hosts = new ArrayList<>();

    @NoFake
    @Builder.Default
    @OrderBy("createdAt DESC")
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FileEntity> files = new ArrayList<>();

    /**
     * @param snsId sns id
     * @param provider provider
     * @param email email
     * @param password password
     * @param name name
     * @param role role
     * @author meteormin
     * @since 2023/09/02
     */
    public static UserEntity create(
            String snsId,
            SocialProvider provider,
            String email,
            String password,
            String name,
            UserRole role) {
        return UserEntity.builder()
                .snsId(snsId)
                .provider(provider)
                .email(email)
                .password(password)
                .name(name)
                .role(role)
                .build();
    }

    /**
     * Updates the password, name, and role of the object.
     *
     * @param password the new password to be set
     * @param name the new name to be set
     * @param role the new role to be set
     */
    public void update(String password, String name, UserRole role) {
        this.password = password;
        this.name = name;
        this.role = role;
    }

    /**
     * Deletes the object.
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void resetPassword(String password) {
        this.password = password;
    }
}
