package com.batch.hott_batch.domain.entity;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberRowMapper implements RowMapper<Member> {

    public static final String ID_COLUMN = "member_seq";
    public static final String NAME_COLUMN = "name";
    public static final String CREATE_DT_COLUMN = "create_dt";
    public static final String LOGIN_DT_COLUMN = "login_dt";
    public static final String IS_ACTIVE_COLUMN = "is_active";

    @Override
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        Member member = new Member();
        member.setSeqNo(rs.getLong(ID_COLUMN));
        member.setName(rs.getString(NAME_COLUMN));
        member.setCreateDt(rs.getDate(CREATE_DT_COLUMN).toLocalDate().atTime(0, 0));
        member.setLoginDt(rs.getDate(LOGIN_DT_COLUMN).toLocalDate().atTime(0, 0));
        member.setIsActive(rs.getBoolean(IS_ACTIVE_COLUMN));
        return member;
    }
}
