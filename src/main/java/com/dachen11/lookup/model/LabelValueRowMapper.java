package com.dachen11.lookup.model;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LabelValueRowMapper implements  RowMapper<LabelValue> {

    @Override
    public LabelValue mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LabelValue(

                rs.getString(1),
                rs.getString(2)

        );
    }
}
