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

    public boolean matchPassword(String rawPassword, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        if (rawPassword == null || passwordEncoder == null || this.password == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, this.password);
    }

    public boolean isAccessible() {
        return this.isApproved;
    }

    public void addPoint(Long amount) {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("적립할 포인트는 0 이상이어야 합니다.");
        }
        this.point = (this.point == null ? 0L : this.point) + amount;
    }

    public void usePoint(Long amount) {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("차감할 포인트는 0 이상이어야 합니다.");
        }
        long current = (this.point == null ? 0L : this.point);
        if (current < amount) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        this.point = current - amount;
    }

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
