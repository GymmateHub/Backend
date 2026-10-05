package com.gymmate.unit.shared.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import com.gymmate.shared.domain.BaseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Base Entity Tests")
class BaseEntityTest {

    // Concrete implementation for testing
    static class TestEntity extends BaseEntity {
    }

    static class TestAuditEntity extends BaseAuditEntity {
    }

    @Nested
    @DisplayName("BaseEntity Tests")
    class BaseEntityTests {

        @Test
        @DisplayName("Should have null ID by default")
        void newEntity_IdIsNull() {
            // Arrange & Act
            TestEntity entity = new TestEntity();

            // Assert
            assertThat(entity.getId()).isNull();
        }

        @Test
        @DisplayName("Should allow setting ID")
        void setId_ShouldUpdateId() {
            // Arrange
            TestEntity entity = new TestEntity();
            UUID id = UUID.randomUUID();

            // Act
            entity.setId(id);

            // Assert
            assertThat(entity.getId()).isEqualTo(id);
        }
    }

    @Nested
    @DisplayName("BaseAuditEntity Tests")
    class BaseAuditEntityTests {

        @Test
        @DisplayName("Should have audit fields")
        void newEntity_HasAuditFields() {
            // Arrange & Act
            TestAuditEntity entity = new TestAuditEntity();

            // Assert - Fields exist (may be null initially)
            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("Should be active by default")
        void newEntity_IsActiveByDefault() {
            // Arrange & Act
            TestAuditEntity entity = new TestAuditEntity();

            // Assert
            assertThat(entity.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should allow deactivation")
        void setActive_False_DeactivatesEntity() {
            // Arrange
            TestAuditEntity entity = new TestAuditEntity();

            // Act
            entity.setActive(false);

            // Assert
            assertThat(entity.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should track created by")
        void setCreatedBy_ShouldStoreUser() {
            // Arrange
            TestAuditEntity entity = new TestAuditEntity();

            // Act
            entity.setCreatedBy("admin@example.com");

            // Assert
            assertThat(entity.getCreatedBy()).isEqualTo("admin@example.com");
        }

        @Test
        @DisplayName("Should track updated by")
        void setUpdatedBy_ShouldStoreUser() {
            // Arrange
            TestAuditEntity entity = new TestAuditEntity();

            // Act
            entity.setUpdatedBy("admin@example.com");

            // Assert
            assertThat(entity.getUpdatedBy()).isEqualTo("admin@example.com");
        }

        @Test
        @DisplayName("Should allow setting timestamps")
        void setTimestamps_ShouldStoreDates() {
            // Arrange
            TestAuditEntity entity = new TestAuditEntity();
            LocalDateTime now = LocalDateTime.now();

            // Act
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);

            // Assert
            assertThat(entity.getCreatedAt()).isEqualTo(now);
            assertThat(entity.getUpdatedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Entities with same ID should be equal")
        void equals_SameId_ReturnsTrue() {
            // Arrange
            UUID id = UUID.randomUUID();
            TestEntity entity1 = new TestEntity();
            TestEntity entity2 = new TestEntity();
            entity1.setId(id);
            entity2.setId(id);

            // Assert
            assertThat(entity1).isEqualTo(entity2);
            assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        }

        @Test
        @DisplayName("Entities with different IDs should not be equal")
        void equals_DifferentId_ReturnsFalse() {
            // Arrange
            TestEntity entity1 = new TestEntity();
            TestEntity entity2 = new TestEntity();
            entity1.setId(UUID.randomUUID());
            entity2.setId(UUID.randomUUID());

            // Assert
            assertThat(entity1).isNotEqualTo(entity2);
        }

        @Test
        @DisplayName("Entity should not equal null")
        void equals_Null_ReturnsFalse() {
            // Arrange
            TestEntity entity = new TestEntity();
            entity.setId(UUID.randomUUID());

            // Assert
            assertThat(entity).isNotEqualTo(null);
        }
    }
}

