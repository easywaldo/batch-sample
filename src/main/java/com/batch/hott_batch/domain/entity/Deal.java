package com.batch.hott_batch.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "deal")
@NoArgsConstructor
@Getter
public class Deal {

    @Id
    @Column(name = "num")
    private String dealNum;

    @Column(name = "hott_id")
    private String hottId;

    @Column(name = "name")
    private String dealName;

    @Builder
    public Deal(String dealNum, String hottId, String dealName) {
        this.dealNum = dealNum;
        this.hottId = hottId;
        this.dealName = dealName;
    }

    public Deal clone() {
        return Deal.builder()
            .dealNum("cd-" + this.dealNum)
            .dealName("복제딜-" + this.dealName)
            .hottId(this.hottId)
            .build();
    }
}