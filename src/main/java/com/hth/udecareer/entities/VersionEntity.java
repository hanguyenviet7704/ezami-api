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
@Table(name = "ez_version")
public class VersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column
    protected String appCode;

    @Column
    protected String platform;

    @Column
    protected String buildNumber;

    @Column
    protected String versionName;

    @Column
    protected boolean forceDownload;

    @Column
    protected String note;

    @Column
    protected boolean latest;

    @Column
    protected boolean enable;
}
