package com.hth.udecareer.entities;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ez_user_purchased")
public class UserPurchasedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    protected Long userId;

    @Column(name = "user_email")
    protected String userEmail;

    @Column(name = "category_code")
    protected String categoryCode;

    @Column(name = "is_purchased")
    protected Integer isPurchased;

    @Column(name = "from_time")
    protected LocalDateTime fromTime;

    @Column(name = "to_time")
    protected LocalDateTime toTime;

    /**
     * JOIN relationship to ez_quiz_category
     * Used for filtering by category title and fetching category details
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code", referencedColumnName = "code", insertable = false, updatable = false)
    private QuizCategoryEntity quizCategory;
}
