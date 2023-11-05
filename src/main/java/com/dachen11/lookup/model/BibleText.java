package com.dachen11.lookup.model;

import com.google.common.base.Joiner;

public class BibleText {
    int verseUid;
    int bookNumber;
    int chapterNumber;
    int verseNumber;
    String verseASV;
    String verseSimp;
    String verseTrad;

    String listSource;
    String scanMode;

    String bookName;



    public static final Joiner PIPE_JOINER = Joiner.on("|").skipNulls();

    public BibleText(int verseUid, int bookNumber, int chapterNumber, int verseNumber,
                     String verseASV, String verseSimp, String verseTrad, String bookName) {
        this.verseUid = verseUid;
        this.bookNumber = bookNumber;
        this.chapterNumber = chapterNumber;
        this.verseNumber =verseNumber;
        this.verseASV = verseASV;
        this.verseSimp = verseSimp;
        this.verseTrad = verseTrad;
        this.bookName = bookName;
    }

    public BibleText() {

    }
    public String getkey()
    {
        String[] keyFields = {String.valueOf(bookNumber),
                String.valueOf(chapterNumber), String.valueOf(verseNumber)};
        return PIPE_JOINER.join(keyFields);
    }

    public int getVerseNumber() {
        return verseNumber;
    }

    public void setVerseNumber(int verseNumber) {
        this.verseNumber = verseNumber;
    }

    public int getVerseUid() {
        return verseUid;
    }

    public void setVerseUid(int verseUid) {
        this.verseUid = verseUid;
    }

    public int getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(int bookNumber) {
        this.bookNumber = bookNumber;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getVerseASV() {
        return verseASV;
    }

    public void setVerseASV(String verseASV) {
        this.verseASV = verseASV;
    }

    public String getVerseSimp() {
        return verseSimp;
    }

    public void setVerseSimp(String verseSimp) {
        this.verseSimp = verseSimp;
    }

    public String getVerseTrad() {
        return verseTrad;
    }

    public void setVerseTrad(String verseTrad) {
        this.verseTrad = verseTrad;
    }

    public String getListSource() {
        return listSource;
    }

    public void setListSource(String listSource) {
        this.listSource = listSource;
    }

    public String getScanMode() {
        return scanMode;
    }

    public void setScanMode(String scanMode) {
        this.scanMode = scanMode;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}


