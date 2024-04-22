package com.meteormin.friday.infrastructure.persistence.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.meteormin.friday.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "login_history")
public class LoginHistoryEntity extends BaseEntity<Long> {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(length = 20)
    @Nullable
    private String ip;

    @Nullable
    @Column(columnDefinition = "timestamp")
    private LocalDateTime deletedAt;

    @ManyToOne
    @JsonBackReference
    private UserEntity user;

    public static LoginHistoryEntity create(UserEntity user, String ip) {
        return LoginHistoryEntity.builder()
                .ip(ip)
                .user(user)
                .build();
    }
}
