package com.dachen11.lookup.helper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.dachen11.lookup.model.BibleText;
import org.apache.commons.csv.CSVFormat;
        import org.apache.commons.csv.CSVParser;
        import org.apache.commons.csv.CSVRecord;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;


public class CSVHelper {
    public static String TYPE = "text/csv";

    static String[] HEADERs = {"Id", "book_name", "book_number", "chapter", "verse", "text"};
//"Verse ID","Book Name","Book Number",Chapter,Verse,Text

    public static final Splitter CQL_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();
    public static final Splitter LINE_SPLITTER = Splitter.on('\n').trimResults().omitEmptyStrings();
    public static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();
    public static final Splitter PIPE_SPLITTER = Splitter.on('|').trimResults().omitEmptyStrings();
    public static final Splitter TILE_SPLITTER = Splitter.on('~').trimResults().omitEmptyStrings();
    public static final Splitter AT_SPLITTER = Splitter.on('^').trimResults().omitEmptyStrings();
    public static final Splitter COLON_SPLITTER = Splitter.on(':').trimResults().omitEmptyStrings();

    public static final Splitter SPACE_SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();

    public static final Joiner CQL_JOINER = Joiner.on("; ").useForNull(",");
    public static final Joiner SPACE_JOINER = Joiner.on(" ").skipNulls();
    public static final Joiner DASH_JOINER = Joiner.on("-").skipNulls();
    public static final Joiner PIPE_JOINER = Joiner.on("|").skipNulls();
    public static final Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();
    public static final Joiner LINE_JOINER = Joiner.on("\n").skipNulls();

    public static List<BibleText> csvToBibleText(
            String fileName, String fileNameSimp, String fileNameTrad)
            throws Exception {

        Map<String, BibleText> bibleTextMap = new HashMap<>();
        Set<String> bookNameSet = new HashSet<>();
        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
//        int num = 0;
        try (final CSVParser parser = CSVParser.parse(new File(fileName),
                StandardCharsets.UTF_8, format)) {
            for (final CSVRecord record : parser) {

                BibleText base = new BibleText();
                base.setVerseUid(Integer.parseInt(record.get(0)));
                //skip 1;
                bookNameSet.add(record.get(1) + " " + record.get(2));
                base.setBookNumber(Integer.parseInt(record.get(2)));
                base.setChapterNumber(Integer.parseInt(record.get(3)));
                base.setVerseNumber(Integer.parseInt(record.get(4)));
                base.setVerseASV(record.get(5));

                String bookKey = base.getkey();
                bibleTextMap.put(bookKey, base);
//                num ++;
//                if (num > 10) break;
            }
        }

//        num = 0;

        try (final CSVParser parser = CSVParser.parse(new File(fileNameSimp),
                StandardCharsets.UTF_8, format)) {
            for (final CSVRecord record : parser) {

                String key = PIPE_JOINER.join(new String[]{record.get(2), record.get(3), record.get(4)});
                BibleText base = bibleTextMap.get(key);
                if (base != null) {
                    base.setVerseSimp(record.get(5));
                } else {
                    System.err.println("simp- no key found" + key);
                }

//                num ++;
//                if (num > 10) break;

            }
        }

//        num = 0;
        try (final CSVParser parser = CSVParser.parse(new File(fileNameTrad),
                StandardCharsets.UTF_8, format)) {
            for (final CSVRecord record : parser) {

                String key = PIPE_JOINER.join(new String[]{record.get(2), record.get(3), record.get(4)});
                BibleText base = bibleTextMap.get(key);
                if (base != null) {
                    base.setVerseTrad(record.get(5));
                } else {
                    System.err.println("trad- no key found" + key);
                }

//                num ++;
//                if (num > 10) break;


            }
        }

        return new ArrayList<>(bibleTextMap.values());

    }

}