package com.hth.udecareer.entities;

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
@Table(name = "ez_favorites_meta",
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_meta", 
                           columnNames = {"favorite_id", "meta_key"})
       },
       indexes = {
           @Index(name = "idx_favorite", columnList = "favorite_id"),
           @Index(name = "idx_meta_key", columnList = "meta_key")
       })
public class FavoriteMetaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "favorite_id", nullable = false)
    private Long favoriteId;

    @Column(name = "meta_key", nullable = false)
    private String metaKey;

    @Column(name = "meta_value", columnDefinition = "LONGTEXT")
    private String metaValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
