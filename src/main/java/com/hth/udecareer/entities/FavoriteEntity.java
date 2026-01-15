package com.hth.udecareer.entities;

import com.hth.udecareer.converter.FavoritableTypeConverter;
import com.hth.udecareer.converter.FavoriteStatusConverter;
import com.hth.udecareer.enums.FavoritableType;
import com.hth.udecareer.enums.FavoriteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ez_favorites", 
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_favorite", 
                           columnNames = {"user_id", "favoritable_type", "favoritable_id"})
       },
       indexes = {
           @Index(name = "idx_user", columnList = "user_id, created_at DESC"),
           @Index(name = "idx_type", columnList = "favoritable_type, favoritable_id"),
           @Index(name = "idx_user_type", columnList = "user_id, favoritable_type"),
           @Index(name = "idx_created", columnList = "created_at DESC"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_user_status", columnList = "user_id, status")
       })
@SqlResultSetMapping(
    name = "FavoriteEntityMapping",
    entities = @EntityResult(
        entityClass = FavoriteEntity.class,
        fields = {
            @FieldResult(name = "id", column = "id"),
            @FieldResult(name = "userId", column = "user_id"),
            @FieldResult(name = "favoritableType", column = "favoritable_type"),
            @FieldResult(name = "favoritableId", column = "favoritable_id"),
            @FieldResult(name = "status", column = "status"),
            @FieldResult(name = "deletedAt", column = "deleted_at"),
            @FieldResult(name = "createdAt", column = "created_at"),
            @FieldResult(name = "updatedAt", column = "updated_at")
        }
    )
)
public class FavoriteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Convert(converter = FavoritableTypeConverter.class)
    @Column(name = "favoritable_type", nullable = false, length = 20)
    private FavoritableType favoritableType;

    @Column(name = "favoritable_id", nullable = false)
    private Long favoritableId;

    @Convert(converter = FavoriteStatusConverter.class)
    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private FavoriteStatus status = FavoriteStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft delete method
     */
    public void softDelete() {
        this.status = FavoriteStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restore from soft delete
     */
    public void restore() {
        this.status = FavoriteStatus.ACTIVE;
        this.deletedAt = null;
    }

    /**
     * Check if this favorite is deleted
     */
    public boolean isDeleted() {
        return this.status == FavoriteStatus.DELETED;
    }
}
