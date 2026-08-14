package com.mk.www.smsmonitor.user.infrastructure;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean isApproved; // 승인 여부

    @Column(nullable = false)
    private Long point; // SSDMA 포인트 (지갑)

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public com.mk.www.smsmonitor.user.domain.User toDomain() {
        return com.mk.www.smsmonitor.user.domain.User.builder()
                .id(this.id)
                .loginId(this.loginId)
                .password(this.password)
                .role(this.role)
                .isApproved(this.isApproved)
                .createdAt(this.createdAt)
                .build();
    }
}
