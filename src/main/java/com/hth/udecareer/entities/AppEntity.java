package com.hth.udecareer.entities;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "ez_app")
public class AppEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column
    protected String appCode;

    @Column
    protected String iosStoreUrl;

    @Column
    protected String androidStoreUrl;

    @Column
    protected boolean enable;
}
