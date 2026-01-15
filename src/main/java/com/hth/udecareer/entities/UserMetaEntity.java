package com.hth.udecareer.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "wp_usermeta")
@Data
public class UserMetaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "umeta_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "meta_key")
    private String metaKey;

    @Column(name = "meta_value")
    private String metaValue;
}
