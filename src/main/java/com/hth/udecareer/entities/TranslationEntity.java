package com.hth.udecareer.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing translations of custom entities (badges, spaces, questions, etc.)
 *
 * This table allows any entity to have multilingual content without modifying
 * the original entity structure.
 *
 * Example usage:
 * - entity_type: "badge", entity_id: 1, field_name: "name", language: "en", translated_value: "First Post"
 * - entity_type: "badge", entity_id: 1, field_name: "name", language: "vi", translated_value: "Bài viết đầu tiên"
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wp_fcom_translations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_translation",
                columnNames = {"entity_type", "entity_id", "field_name", "language"}
        ),
        indexes = {
                @Index(name = "idx_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_language", columnList = "language")
        }
)
public class TranslationEntity {

    // Entity type constants
    public static final String TYPE_BADGE = "badge";
    public static final String TYPE_SPACE = "space";
    public static final String TYPE_TOPIC = "topic";
    public static final String TYPE_QUESTION = "question";
    public static final String TYPE_NOTIFICATION = "notification";
    public static final String TYPE_CERTIFICATION = "certification";
    public static final String TYPE_SKILL = "skill";

    // Field name constants
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_MESSAGE = "message";

    // Language constants
    public static final String LANG_VI = "vi";
    public static final String LANG_EN = "en";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of entity being translated (badge, space, question, etc.)
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * ID of the entity being translated
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Name of the field being translated (name, description, title, etc.)
     */
    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;

    /**
     * Language code (vi, en)
     */
    @Column(name = "language", nullable = false, length = 5)
    private String language;

    /**
     * The translated value
     */
    @Column(name = "translated_value", columnDefinition = "TEXT")
    private String translatedValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
