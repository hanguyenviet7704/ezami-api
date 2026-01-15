package com.hth.udecareer.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "wp_learndash_user_activity_meta")
public class UserActivityMetaEntity {
    @Id
    @Column(name = "activity_meta_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "activity_meta_key")
    private String activityMetaKey;

    @Column(name = "activity_meta_value")
    private String activityMetaValue;
}
