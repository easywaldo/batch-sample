package com.batch.hott_batch.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @Column(name = "member_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seqNo;

    @Column(name = "name")
    private String name;

    @Column(name = "create_dt")
    private LocalDateTime createDt;

    @Column(name = "login_dt")
    private LocalDateTime loginDt;

    @Column(name = "is_active")
    private Boolean isActive;

    @Builder
    public Member(Long seqNo, String name, LocalDateTime createDt, LocalDateTime loginDt, Boolean isActive) {
        this.seqNo = seqNo;
        this.name = name;
        this.createDt = createDt;
        this.loginDt = loginDt;
        this.isActive = isActive;
    }

    public Member setInActive() {
        this.isActive = loginDt.isBefore(LocalDateTime.now().minusDays(365));
        return this;
    }
}
