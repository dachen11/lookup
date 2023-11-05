package com.dachen11.lookup.search;

import com.dachen11.lookup.helper.CSVHelper;
import com.dachen11.lookup.model.BaseList;
import com.dachen11.lookup.model.BibleText;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.dachen11.lookup.helper.CSVHelper.COMMA_SPLITTER;
import static com.dachen11.lookup.helper.CSVHelper.SPACE_SPLITTER;

/**
 * Index and search bible verse using Apache Lucene api
 */
public class SearchService {

    private Analyzer analyzer = new StandardAnalyzer();;
    private IndexReader readerBible;
    private IndexSearcher searcherBible;

    public IndexSearcher getSearcherBible() {
        return searcherBible;
    }

    private final String rootPath;

    private final String[] bookArray = {
            "Genesis",
            "Exodus",
            "Leviticus",
            "Numbers",
            "Deuteronomy",
            "Joshua",
            "Judges",
            "Ruth",
            "1 Samuel",
            "2 Samuel",
            "1 Kings",
            "2 Kings",
            "1 Chronicles",
            "2 Chronicles",
            "Ezra",
            "Nehemiah",
            "Esther",
            "Job",
            "Psalms",
            "Proverbs",
            "Ecclesiastes",
            "Song of Solomon",
            "Isaiah",
            "Jeremiah",
            "Lamentations",
            "Ezekiel",
            "Daniel",
            "Hosea",
            "Joel",
            "Amos",
            "Obadiah",
            "Jonah",
            "Micah",
            "Nahum",
            "Habakkuk",
            "Zephaniah",
            "Haggai",
            "Zechariah",
            "Malachi",
            "Matthew",
            "Mark",
            "Luke",
            "John",
            "Acts",
            "Romans",
            "1 Corinthians",
            "2 Corinthians",
            "Galatians",
            "Ephesians",
            "Philippians",
            "Colossians",
            "1 Thessalonians",
            "2 Thessalonians",
            "1 Timothy",
            "2 Timothy",
            "Titus",
            "Philemon",
            "Hebrews",
            "James",
            "1 Peter",
            "2 Peter",
            "1 John",
            "2 John",
            "3 John",
            "Jude",
            "Revelation"
    };

    public SearchService(String rootPath) {
        this.rootPath = rootPath;
        initSearch();
    }





    @PreDestroy
    public void stop()
            throws Exception {

        closeIndexReader();


        //todo sdn need to close also
    }

    /**
     * Close index reader.
     */
    public void closeIndexReader() {
        IndexUtils.close(readerBible);

    }

    /**
     *
     * @param directory
     * @return true if the directory is empty
     * @throws IOException
     */
    protected boolean isDirEmpty(final Path directory) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    /**
     * Open Reader and IndexSearcher and get ready for search
     */
    public void initSearch() {


        try {

            Path indexPath = Paths.get(rootPath);
            if (isDirEmpty(indexPath)) {
                System.err.println("index is empty, skip search init");
                return;
            }

            readerBible = DirectoryReader.open(FSDirectory.open(indexPath));

            searcherBible = new IndexSearcher(readerBible);



        } catch (IOException e) {

            System.err.println("error on init search" + e.getMessage());
            throw new RuntimeException("error on init search" + e.getMessage());
        }
    }

    /**
     * Index the bibleText into Lucene Index for quick search.
     * @param bibleTextList
     * @return baseList
     */
    public BaseList IndexList(List<BibleText> bibleTextList) {
        BaseList baseList = new BaseList();
        int errNumber = 0;
        int lineNumber = 0;
        int count = 0;
        String indexPath = rootPath;
        try {
            System.out.println("indexing to directory " + indexPath);
            Directory dirBible = FSDirectory.open(Paths.get(indexPath));

            IndexWriterConfig iwcBible = new IndexWriterConfig(analyzer);
            iwcBible.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writerBible = new IndexWriter(dirBible, iwcBible);
            for (BibleText base : bibleTextList) {
                lineNumber++;
                int retCount = createDocumentBible(base, writerBible, lineNumber, analyzer);
                if (retCount > -1) {
                    count += retCount;
                    if (count > 1 && count % 1000 == 1) {
                        writerBible.commit();
                        System.out.println("write to index count= " + count + "  line number=" + lineNumber);
                    }
                } else {
                    errNumber++;
                    if (errNumber > 100) {
                        String error = String.format("error in indexing too many errors: error count=%d, record count=%dd",
                                errNumber, count);
                        throw new RuntimeException(error);
                    }
                }
            } //end of for loop

            //last one
            System.out.println("write to index count= " + count + "  line number=" + lineNumber);
            closeIndexWriter(writerBible, dirBible);

        } catch (Exception e) {
            String error = " error on index bible";
            System.err.println(error);
            throw new RuntimeException(error + e.getMessage());

        }
        baseList.setTotalRow(lineNumber);
        baseList.setStartRow(errNumber);
        baseList.setStartRow(count);
        initSearch();
        return baseList;
    }

    public int createDocumentBible(BibleText base, IndexWriter writerBible, int lineNumber, Analyzer analyzer) {
        int countBible = 0;
        try {
            Document doc = new Document();
            doc.add(new StringField("uid", String.valueOf(base.getVerseUid()), Field.Store.YES));
            doc.add(new StringField("book_number_str", String.valueOf(base.getBookNumber()), Field.Store.YES));
            doc.add(new StringField("chapter_number_str", String.valueOf(base.getChapterNumber()), Field.Store.YES));
            doc.add(new StringField("verse_number_str", String.valueOf(base.getVerseNumber()), Field.Store.YES));
            doc.add(new StringField("verse_simple", String.valueOf(base.getVerseSimp()), Field.Store.YES));
            doc.add(new StringField("verse_trad", String.valueOf(base.getVerseTrad()), Field.Store.YES));
            doc.add(new StringField("verse_asv", String.valueOf(base.getVerseASV()), Field.Store.YES));

            doc.add(new IntPoint("book_number", base.getBookNumber()));
            List<String> tokenList = analyzeText(analyzer, base.getVerseASV());
            String termStr = CSVHelper.SPACE_JOINER.join(tokenList);
            Field nameField = new TextField("name", termStr, Field.Store.YES);
            doc.add(nameField);
            writerBible.addDocument(doc);
            countBible++;

        } catch (Exception e) {
            System.err.println("error on adding doc" + lineNumber);
            return -1;
        }
        return countBible;
    }

    public List<String> analyzeText(Analyzer analyzer, String text) {
        Objects.requireNonNull(text);
        try {
            List<String> result = new ArrayList<>();
            Set<String> termSet = new HashSet<>();
            TokenStream stream = analyzer.tokenStream("", text);
            stream.reset();
            CharTermAttribute charAtt = stream.getAttribute(CharTermAttribute.class);
            while (stream.incrementToken()) {
                String term = charAtt.toString();
                while (termSet.contains(term)) {
                    term = term + "zzz";
                    System.out.println("duplicate word:" + term);
//                    LOG.info("duplicate word:" + term);
                }
                result.add(term);
                termSet.add(term);

            }
            stream.close();
            return result;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe);

        }
    }

    protected void closeIndexWriter(IndexWriter writer, Directory dir)
            throws IOException {
        writer.commit();
        writer.close();
        IndexUtils.close(dir);
    }

    public BaseList freeTextSearch(BibleText base, int maxRow) {
        BaseList baseList = new BaseList();
        try {

            int startRow = 0;

            //add Feb 28, 2021
            Float minScore = 0.1f;


            List<BibleText> resultList = searchWatchList(base, "name", maxRow);

            baseList.setTotalRow(resultList.size());

            baseList.setEntities(resultList);
        } catch (IOException e) {
            System.err.println("error on freetext search.query" + e.getMessage());
            throw new RuntimeException("error on sdnNameServer query:" + e.getMessage());
        }
        return baseList;
    }

    public BibleText convertDocToBibleText(Document doc)
    {
        BibleText base = new BibleText();
        base.setVerseUid(Integer.parseInt(doc.get("uid")));
        base.setBookNumber(Integer.parseInt(doc.get("book_number_str")));
        base.setChapterNumber(Integer.parseInt(doc.get("chapter_number_str")));
        base.setVerseNumber(Integer.parseInt(doc.get("verse_number_str")));
        base.setVerseASV(doc.get("verse_asv"));
        base.setVerseSimp(doc.get("verse_simple"));
        base.setVerseTrad(doc.get("verse_trad"));
        base.setBookName(bookArray[base.getBookNumber()-1]);
        return base;


    }
    public List<BibleText> searchWatchList(BibleText base, String field, int maxAlert)
            throws IOException {
        List<BibleText> retList = new ArrayList<>();
        String query = base.getVerseASV();
        BooleanQuery.Builder booleanAB = new BooleanQuery.Builder();
        if (query.contains("*") || query.contains("?")) {

            List<String> termList = SPACE_SPLITTER.splitToList(query);
            for (String term : termList) {
                if (term.contains("*") || term.contains("?")) {
                    booleanAB.add(new WildcardQuery(new Term(field, term)), BooleanClause.Occur.MUST);
                } else {

                    booleanAB.add(new TermQuery(new Term(field, term)), BooleanClause.Occur.MUST);
                }
            }
        } else {

            List<String> termList = analyzeText(analyzer, query);  //will be no duplicated.

            for (String term : termList) {
                booleanAB.add(new TermQuery(new Term(field, term)), BooleanClause.Occur.MUST);
            }
        }

        String listSource = base.getListSource();
        if(listSource!=null) {
            List<String> listSourceList = COMMA_SPLITTER.splitToList(listSource);
            if (listSourceList.size() > 0) {
                List<Integer> bookList = new ArrayList<>(listSourceList.size());
                for (String str : listSourceList) {
                    bookList.add(Integer.parseInt(str));
                }
                Query intQuery = IntPoint.newSetQuery("book_number", bookList);
                booleanAB.add(intQuery, BooleanClause.Occur.FILTER);
            }
        }
        //

        TopDocs topDocsSdn = searcherBible.search(booleanAB.build(), maxAlert);
        StoredFields storedFieldsSdn = searcherBible.storedFields();
        for (ScoreDoc hit : topDocsSdn.scoreDocs) {
            Document doc = storedFieldsSdn.document(hit.doc);
            BibleText bibleText = convertDocToBibleText(doc);
            retList.add(bibleText);
        }
        return retList;
    }


}
