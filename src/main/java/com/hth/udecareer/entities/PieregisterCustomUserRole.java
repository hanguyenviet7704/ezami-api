package com.hth.udecareer.entities;

import javax.persistence.*;

import lombok.Data;

@Data
@Entity
@Table(name = "wp_pieregister_custom_user_roles")
public class PieregisterCustomUserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_key")
    private String roleKey;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "wp_role_name")
    private String wpRoleName;
}
