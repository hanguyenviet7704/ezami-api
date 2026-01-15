package com.hth.udecareer.eil.entities;

import com.hth.udecareer.eil.enums.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "eil_ai_feedback")
public class EilAiFeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "feedback_type", nullable = false, length = 30)
    private String feedbackType;

    @Column(name = "context_type", length = 30)
    private String contextType;

    @Column(name = "context_id")
    private Long contextId;

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "vi";

    @Column(name = "feedback_json", nullable = false, columnDefinition = "JSON")
    private String feedbackJson;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "recommendations", columnDefinition = "JSON")
    private String recommendations;

    @Column(name = "action_items", columnDefinition = "JSON")
    private String actionItems;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "prompt_version")
    @Builder.Default
    private Integer promptVersion = 1;

    @Column(name = "tokens_used")
    @Builder.Default
    private Integer tokensUsed = 0;

    @Column(name = "generation_time_ms")
    @Builder.Default
    private Integer generationTimeMs = 0;

    @Column(name = "user_rating")
    private Integer userRating;

    @Column(name = "user_comment", columnDefinition = "TEXT")
    private String userComment;

    @Column(name = "is_helpful")
    private Boolean isHelpful;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public FeedbackType getFeedbackTypeEnum() {
        return FeedbackType.fromCode(feedbackType);
    }

    public void setFeedbackTypeEnum(FeedbackType type) {
        this.feedbackType = type.getCode();
    }
}
