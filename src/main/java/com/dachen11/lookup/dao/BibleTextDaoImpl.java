package com.dachen11.lookup.dao;

import com.dachen11.lookup.model.BibleText;
import com.dachen11.lookup.model.BibleTextRowMapper;
import com.dachen11.lookup.model.LabelValue;
import com.dachen11.lookup.model.LabelValueRowMapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BibleTextDaoImpl implements BibleTextDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public BibleTextDaoImpl(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * get 10 records for testing
     *
     * @return List of BibleText
     */
    @Override
    public List<BibleText> getTop10() {
        String sql = """
                select verse_uid, book_number, chapter_number, 
                verse_number, verse_asv, verse_simp, verse_trad
                from bible_txt where rownum < 11
                """;
        return this.jdbcTemplate.query(sql, new BibleTextRowMapper());
    }

    /**
     * get the referenceMap used by the front end for dropdown (select options).
     *
     * @return Map of LabelValue List.
     */
    @Override
    public Map<String, List<LabelValue>> getRefValueList() {
        Map<String, List<LabelValue>> rv = new HashMap<>();

        String sql = " select book_number, book_name from bible_book_list order by 1";

        List<LabelValue> bookList = this.jdbcTemplate.query(sql, new LabelValueRowMapper());
        for (LabelValue book : bookList) {
            if (book.getValue().length() == 1) {
                book.setValue("0" + book.getValue());
            }
        }
        rv.put("bookListType", bookList);

        sql = " select distinct book_number, chapter_number from bible_txt order by 1, 2";

        List<LabelValue> bookChapterList = this.jdbcTemplate.query(sql, new LabelValueRowMapper());
        for (LabelValue bookChapter : bookChapterList) {
            if (bookChapter.getValue().length() == 1) {
                bookChapter.setValue("0" + bookChapter.getValue() + "-" + bookChapter.getLabel());
            } else {
                bookChapter.setValue(bookChapter.getValue() + "-" + bookChapter.getLabel());

            }

            bookChapter.setLabel("Chapter-" + bookChapter.getLabel());
        }

        rv.put("bookChapterType", bookChapterList);

        return rv;

    }

    /**
     * read the 3 csv files, convent them into list of BibleText, then batch insert into
     * bible_txt table in the database.
     *
     * @param verseList List of BibleText
     */
    @Override
    public void batchInsert(List<BibleText> verseList) {
        String sql = """
                INSERT INTO bible_txt(verse_uid, book_number, chapter_number, 
                verse_number, verse_asv, verse_simp, verse_trad) 
                VALUES (?,?,?,?,?,?,?)
                """;
//        return this.jdbcTemplate.update(sql, employee.getFirstName(),
//                employee.getLastName(), employee.getEmail());
//        return verseList.size();

        final int batchSize = 500;
        List<List<BibleText>> batchLists = Lists.partition(verseList, batchSize);

        for (List<BibleText> batch : batchLists) {
            this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i)
                        throws SQLException {
                    BibleText base = batch.get(i);
                    ps.setInt(1, base.getVerseUid());
                    ps.setInt(2, base.getBookNumber());
                    ps.setInt(3, base.getChapterNumber());
                    ps.setInt(4, base.getVerseNumber());
                    ps.setString(5, base.getVerseASV());
                    ps.setString(6, base.getVerseSimp());
                    ps.setString(7, base.getVerseTrad());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    public boolean containsHanScript(String s) {
        return s.codePoints().anyMatch(
                codepoint ->
                        Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN);
    }

    /**
     * Give the search criteria, return list of BibleText
     *
     * @param base BibleText
     * @return List of BibleText
     */
    @Override
    public List<BibleText> findByQuery(BibleText base) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT *  from ( ");
        sb.append("  SELECT a.*, ROWNUM rnum  from ( ");
        sb.append("""
                select verse_uid, book_number, chapter_number, 
                verse_number, verse_asv, verse_simp, verse_trad, book_name
                from v_bible_txt where 1=1
                """);
        Map<String, Object> params = new HashMap<>();
        if (base.getListSource() != null && base.getListSource().trim().length() > 0) {
            sb.append(" and book_number in (").append(base.getListSource()).append(") ");
        }

        if (base.getBookNumber() > 0) {
            sb.append(" and book_number =  :book_number");
            params.put("book_number", base.getBookNumber());
        }
        if (base.getChapterNumber() > 0) {
            sb.append(" and chapter_number = :chapter_number");
            params.put("chapter_number", base.getChapterNumber());
        }
        if (base.getVerseASV() != null) {
            if(containsHanScript(base.getVerseASV())) {
                sb.append(" and (verse_simp like :verse_simp or verse_trad like :verse_trad ) ");
                params.put("verse_simp", "%" + base.getVerseASV() + "%");
                params.put("verse_trad", "%" + base.getVerseASV() + "%");
            }
            else {
                sb.append(" and lower(verse_asv) like :verse_asv ");
                params.put("verse_asv", "%" + base.getVerseASV().toLowerCase() + "%");
            }
        }

        sb.append(" order by verse_uid ");

        sb.append("   ) a where ROWNUM <= :maxRow "); //MAX_ROW
        sb.append(" )  where rnum >= :minRow ");  //MIN_ROW

        params.put("maxRow", 500);
        params.put("minRow", 0);
        List<BibleText> retList = this.namedParameterJdbcTemplate.query(sb.toString(), params,
                new BibleTextRowMapper());

        return retList;
    }

}
