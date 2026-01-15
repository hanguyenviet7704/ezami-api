package com.hth.udecareer.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "wp_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_login")
    protected String username;

    @Column(name = "user_pass")
    protected String password;

    @Column(name = "user_nicename")
    protected String niceName;

    @Column(name = "user_email")
    protected String email;

    @Column(name = "user_url")
    protected String userUrl;

    @Column(name = "user_registered")
    protected LocalDateTime registeredDate;

    @Column(name = "user_activation_key")
    protected String activationKey;

    @Column(name = "user_status")
    protected Integer status;

    @Column(name = "display_name")
    protected String displayName;

    @Column(name = "app_code")
    protected String appCode;
}
