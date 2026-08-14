package com.mk.www.smsmonitor.category.infrastructure.persistence;

import com.mk.www.smsmonitor.category.domain.SpendingCategory;
import com.mk.www.smsmonitor.category.domain.SpendingCategoryRepository;
import com.mk.www.smsmonitor.user.infrastructure.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({SpendingCategoryRepositoryImpl.class, SpendingCategoryMapper.class})
class SpendingCategoryRepositoryTest {

    @Autowired
    private SpendingCategoryRepository spendingCategoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .loginId("test-user")
                .password("password")
                .role("ROLE_USER")
                .isApproved(true)
                .point(0L)
                .build();
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("카테고리_엔티티를_성공적으로_저장하고_조회한다")
    void 카테고리_엔티티를_성공적으로_저장하고_조회한다() {
        // given
        SpendingCategory newCategory = SpendingCategory.builder()
                .userId(testUser.getId()) // 유저 아이디 추가
                .name("식비")
                .isStupidCostTarget(true)
                .build();

        // when
        SpendingCategory savedCategory = spendingCategoryRepository.save(newCategory);

        // then
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("식비");
        assertThat(savedCategory.isStupidCostTarget()).isTrue();
    }

    @Test
    @DisplayName("이름으로_카테고리를_성공적으로_조회한다")
    void 이름으로_카테고리를_성공적으로_조회한다() {
        // given
        SpendingCategory newCategory = SpendingCategory.builder()
                .userId(testUser.getId()) // 유저 아이디 추가
                .name("교통비")
                .isStupidCostTarget(false)
                .build();
        spendingCategoryRepository.save(newCategory);

        // when
        SpendingCategory foundCategory = spendingCategoryRepository.findByName("교통비").orElse(null);

        // then
        assertThat(foundCategory).isNotNull();
        assertThat(foundCategory.getName()).isEqualTo("교통비");
    }
}
