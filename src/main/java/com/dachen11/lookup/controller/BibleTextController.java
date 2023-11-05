package com.dachen11.lookup.controller;

import com.dachen11.lookup.dao.BibleTextDao;
import com.dachen11.lookup.helper.CSVHelper;
import com.dachen11.lookup.model.BaseList;
import com.dachen11.lookup.model.BibleText;
import com.dachen11.lookup.model.LabelValue;
import com.dachen11.lookup.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("api/bible")
public class BibleTextController {
    private final BibleTextDao bibleTextDao;
    private SearchService searchService;

    @Autowired
    public BibleTextController(BibleTextDao bibleTextDao) {
        this.bibleTextDao = bibleTextDao;
        System.out.println("indexpath= " + bibleIndexPath);
//        this.searchService = new SearchService(bibleIndexPath);
    }

    @Value("${bible_max_return}")
    private int maxRow = 100;

    @Value("${bible_file_path}")
    private String bibleFilePath;

    @Value("${bible_index_path}")
    private String bibleIndexPath;

    @Value("${bible_asv}")
    private String bibleASV;

    @Value("${bible_chinese_simp}")
    private String bibleSimp;

    @Value("${bible_chinese_trad}")
    private String bibleTrad;

    /**
     * Get the first 10 records for test connectivity only
     * @return List of BibleText
     */
    @GetMapping("/get10")
    public List<BibleText> getTop10() {
        return bibleTextDao.getTop10();
    }

    /**
     * get the referenceMap used by the front end for dropdown (select options).
     *
     * @return Map of LabelValue List.
     */
    @GetMapping("/getRefListValue")
    public Map<String, List<LabelValue>> getRefValueList() {
        return bibleTextDao.getRefValueList();
    }

    @GetMapping("/hello")
    public String HelloWorld() {
        return "hello there!";
    }

    /**
     * Give the search criteria, return list of BibleText
     * free format text search with Lucene Index.
     *
     * @param payload
     * @return List of BibleText
     */
    @PostMapping("/query")
    public List<BibleText> queryBibleText(@RequestBody Map<String, Object> payload) { //BibleText bibleText
        String name = (String) payload.get("name");
        String listSource = (String) payload.get("listSource");
        String scanMode = (String) payload.get("scanMode");
        BibleText bibleText = new BibleText();
        bibleText.setVerseASV(name);
        bibleText.setListSource(listSource);
        bibleText.setScanMode(scanMode);

        if(Objects.equals(scanMode, "sql") || containsHanScript(name)) {
            return bibleTextDao.findByQuery(bibleText);
        }

        if(searchService==null) {
            searchService = new SearchService(bibleIndexPath);
            searchService.initSearch();
        }
        BaseList baseList =searchService.freeTextSearch(bibleText, maxRow);
        return (List<BibleText>) baseList.getEntities();
    }

    public boolean containsHanScript(String s) {
        return s.codePoints().anyMatch(
                codepoint ->
                        Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN);
    }
    /**
     * Give the search criteria, return list of BibleText
     * Look up by book number and chapter number only
     *
     * @param payload
     * @return List of BibleText
     */
    @PostMapping("/lookup")
    public List<BibleText> lookupBibleText(@RequestBody Map<String, Object> payload) { //BibleText bibleText
        String book = (String) payload.get("bookNumber");
        String chapter = (String) payload.get("chapterNumber");
//        String scanMode = (String)payload.get("scanMode");
        int bookNumber = 1;
        int chapterNumber = 1;
        try {
            bookNumber = Integer.parseInt(book);
            chapterNumber = Integer.parseInt(chapter.substring(3));
        } catch (Exception e) {
            e.printStackTrace();
        }

        BibleText bibleText = new BibleText();
//        bibleText.setVerseASV(name);
        bibleText.setBookNumber(bookNumber); //hack here.
        bibleText.setChapterNumber(chapterNumber);

        return bibleTextDao.findByQuery(bibleText);
    }

    @GetMapping("/load")
    public BaseList loadBible() throws Exception {
        BaseList base = new BaseList();
        String fileNameASV = bibleFilePath + bibleASV;
        String fileNameSimp = bibleFilePath + bibleSimp;
        String fileNameTrad = bibleFilePath + bibleTrad;
        List<BibleText> bibleTextList = CSVHelper.csvToBibleText(fileNameASV, fileNameSimp, fileNameTrad);

        bibleTextDao.batchInsert(bibleTextList);
        base.setTotalRow(bibleTextList.size());
        return base;
    }

    @GetMapping("/indexBible")
    public BaseList indexBible() throws Exception {

        String fileNameASV = bibleFilePath + bibleASV;
        String fileNameSimp = bibleFilePath + bibleSimp;
        String fileNameTrad = bibleFilePath + bibleTrad;
        List<BibleText> bibleTextList = CSVHelper.csvToBibleText(fileNameASV, fileNameSimp, fileNameTrad);

        if (searchService == null) {
            searchService = new SearchService(bibleIndexPath);
        }
        BaseList baseList = searchService.IndexList(bibleTextList);
        return baseList;
    }

}
