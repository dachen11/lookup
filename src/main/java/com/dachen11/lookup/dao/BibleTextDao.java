package com.dachen11.lookup.dao;

import com.dachen11.lookup.model.BibleText;
import com.dachen11.lookup.model.LabelValue;
import com.google.common.collect.ListMultimap;

import java.util.List;
import java.util.Map;

public interface BibleTextDao {
    /**
     * get 10 records for testing
     *
     * @return List of BibleText
     */
    List<BibleText> getTop10();

    /**
     * get the referenceMap used by the front end for dropdown (select options).
     *
     * @return Map of LabelValue List.
     */
    Map<String, List<LabelValue>> getRefValueList();

    /**
     * read the 3 csv files, convent them into list of BibleText, then batch insert into
     * bible_txt table in the database.
     *
     * @param verseList List of BibleText
     */
    void batchInsert(List<BibleText> verseList);

    /**
     * Give the search criteria, return list of BibleText
     *
     * @param base BibleText
     * @return List of BibleText
     */
    List<BibleText> findByQuery(BibleText base);


}
