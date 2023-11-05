package com.dachen11.lookup.model;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BibleTextRowMapper implements  RowMapper<BibleText> {

    @Override
    public BibleText mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new BibleText(
                rs.getInt(1),
                rs.getInt(2),
                rs.getInt(3),
                rs.getInt(4),
                rs.getString(5),
                rs.getString(6),
                rs.getString(7),
                rs.getString(8)
        );
    }
}
