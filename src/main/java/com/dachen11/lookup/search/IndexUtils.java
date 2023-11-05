package com.dachen11.lookup.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.MultiBits;
import org.apache.lucene.index.MultiDocValues;
import org.apache.lucene.index.MultiTerms;
import org.apache.lucene.index.NoDeletionPolicy;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IndexUtils
{
   
    /**
     * Opens index(es) reader for given index path.
     *
     * @param indexPath - path to the index directory
    //     * @param dirImpl - already use directory reader;
     * @return index reader
     * @throws Exception - if there is a low level IO error.
     */
    public static IndexReader openIndex(String indexPath) throws Exception {
        final Path path = FileSystems.getDefault().getPath(Objects.requireNonNull(indexPath));
        Directory dir = FSDirectory.open(path);
        DirectoryReader reader = DirectoryReader.open(dir);
        System.out.println(String.format(Locale.ENGLISH,
                "IndexReader successfully opened. Index path=%s", indexPath));
        return reader;

    }

    /**
     * Close index directory.
     *
     * @param dir - index directory to be closed
     */
    public static void close(Directory dir) {
        try {
            if (dir != null) {
                dir.close();
                System.out.println("Directory successfully closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing directory" +  e.getMessage());
        }
    }

    /**
     * Close index reader.
     *
     * @param reader - index reader to be closed
     */
    public static void close(IndexReader reader) {
        try {
            if (reader != null) {
                reader.close();
                System.out.println("IndexReader successfully closed.");
                if (reader instanceof DirectoryReader) {
                    Directory dir = ((DirectoryReader) reader).directory();
                    dir.close();
                    System.out.println("Directory successfully closed.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error closing index reader" +  e.getMessage());
        }
    }

    /**
     * Create an index writer.
     *
     * @param dir - index directory
     * @param analyzer - analyzer used by the index writer
     * @param useCompound - if true, compound index files are used
     * @param keepAllCommits - if true, all commit generations are kept
     * @return new index writer
     * @throws IOException - if there is a low level IO error.
     */
    public static IndexWriter createWriter(
            Directory dir, Analyzer analyzer, boolean useCompound, boolean keepAllCommits)
            throws IOException {
        return createWriter(Objects.requireNonNull(dir), analyzer, useCompound, keepAllCommits, null);
    }

    /**
     * Create an index writer.
     *
     * @param dir - index directory
     * @param analyzer - analyser used by the index writer
     * @param useCompound - if true, compound index files are used
     * @param keepAllCommits - if true, all commit generations are kept
     * @param ps - information stream
     * @return new index writer
     * @throws IOException - if there is a low level IO error.
     */
    public static IndexWriter createWriter(
            Directory dir, Analyzer analyzer, boolean useCompound, boolean keepAllCommits, PrintStream ps)
            throws IOException {
        Objects.requireNonNull(dir);

        IndexWriterConfig config =
                new IndexWriterConfig(analyzer == null ? new StandardAnalyzer() : analyzer);
        config.setUseCompoundFile(useCompound);
        if (ps != null) {
            config.setInfoStream(ps);
        }
        if (keepAllCommits) {
            config.setIndexDeletionPolicy(NoDeletionPolicy.INSTANCE);
        } else {
            config.setIndexDeletionPolicy(new KeepOnlyLastCommitDeletionPolicy());
        }

        return new IndexWriter(dir, config);
    }

    /**
     * Collect all terms and their counts in the specified fields.
     *
     * @param reader - index reader
     * @param fields - field names
     * @return a map contains terms and their occurrence frequencies
     * @throws IOException - if there is a low level IO error.
     */
    public static Map<String, Long> countTerms(IndexReader reader, Collection<String> fields)
            throws IOException {
        Map<String, Long> res = new HashMap<>();
        for (String field : fields) {
            if (!res.containsKey(field)) {
                res.put(field, 0L);
            }
            Terms terms = MultiTerms.getTerms(reader, field);
            if (terms != null) {
                TermsEnum te = terms.iterator();
                while (te.next() != null) {
                    res.put(field, res.get(field) + 1);
                }
            }
        }
        return res;
    }

    /**
     * Returns the {@link Bits} representing live documents in the index.
     *
     * @param reader - index reader
     */
    public static Bits getLiveDocs(IndexReader reader) {
        if (reader instanceof LeafReader) {
            return ((LeafReader) reader).getLiveDocs();
        } else {
            return MultiBits.getLiveDocs(reader);
        }
    }

    /**
     * Returns field {@link FieldInfos} in the index.
     *
     * @param reader - index reader
     */
    public static FieldInfos getFieldInfos(IndexReader reader) {
        if (reader instanceof LeafReader) {
            return ((LeafReader) reader).getFieldInfos();
        } else {
            return FieldInfos.getMergedFieldInfos(reader);
        }
    }

    /**
     * Returns the {@link FieldInfo} referenced by the field.
     *
     * @param reader - index reader
     * @param fieldName - field name
     */
    public static FieldInfo getFieldInfo(IndexReader reader, String fieldName) {
        return getFieldInfos(reader).fieldInfo(fieldName);
    }

    /**
     * Returns all field names in the index.
     *
     * @param reader - index reader
     */
    public static Collection<String> getFieldNames(IndexReader reader) {
        return StreamSupport.stream(getFieldInfos(reader).spliterator(), false)
                .map(f -> f.name)
                .collect(Collectors.toList());
    }

    /**
     * Returns the {@link Terms} for the specified field.
     *
     * @param reader - index reader
     * @param field - field name
     * @throws IOException - if there is a low level IO error.
     */
    public static Terms getTerms(IndexReader reader, String field) throws IOException {
        if (reader instanceof LeafReader) {
            return ((LeafReader) reader).terms(field);
        } else {
            return MultiTerms.getTerms(reader, field);
        }
    }


    private IndexUtils() {}
}
