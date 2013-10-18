package org.simplyphi.pbms.lucene;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.simplyphi.pbms.models.Link;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: umar
 * Date: 30/9/13
 * Time: 1:00 AM
 * To change this template use File | Settings | File Templates.
 */
public enum LuceneIndex {
    INSTANCE;
    private static final Logger logger = Logger.getLogger(LuceneIndex.class);
    private static final String indexPath = "bm-data";
    private static IndexWriter writer;
    private static StandardAnalyzer analyzer;
    private static Directory index;
    private static IndexWriterConfig iwc;
    public static SearcherManager smgr;
    public static final String FN_URL = "url";
    public static final String FN_TITLE = "title";
    public static final String FN_MODIFIED = "modified";
    public static final String FN_TAGS = "tags";
    public static final String FN_TEXT = "text";

    //public LuceneIndex() throws IOException
    static {
        logger.info("Initializing index: ");
        analyzer = new StandardAnalyzer(Version.LUCENE_45);
        try {
            index = FSDirectory.open(new File(indexPath));
            iwc = new IndexWriterConfig(Version.LUCENE_45, analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            writer = new IndexWriter(index, iwc);
            smgr = new SearcherManager(writer, true, null);
        } catch (IOException e) {
            logger.fatal("Exception Initializing index: ", e);
        }
        logger.info("Index Initialized Successfully: ");

    }

    /**
     * Deletes Documents which matches to specified field:value
     * @param field
     * @param value
     * @throws IOException
     */
    private static void deleteBookmarkByField(String field, String value) throws IOException {
        Term term = new Term(field, value);
        writer.deleteDocuments(term);
        writer.commit();
        smgr.maybeRefreshBlocking();
    }

    /**
     * Deletes bookmarks matching to tag
     * @param tag  : name of tag
     * @throws IOException
     */
    public static void deleteBookmarkByTag(String tag) throws IOException {
        deleteBookmarkByField(FN_TAGS, tag.trim().toLowerCase());
    }

    /**
     * Deletes Bookmark matching to given url
     * @param url
     * @throws IOException
     */
    public static void deleteBookmark(String url) throws IOException {
        deleteBookmarkByField(FN_URL, url);
    }

    /**
     * add new Bookmark to lucene index
     * @param title  : title of web page
     * @param url    : url of page
     * @param tags   : tags of page
     * @throws IOException when something bad happens
     */
    public static void addBookmark(String title, String url, String tags) throws IOException {
        Document doc = new Document();
        StringBuffer textBuf = new StringBuffer(title);
        doc.add(new TextField(FN_TITLE, title, Field.Store.YES));
        doc.add(new StringField(FN_URL, url, Field.Store.YES));
        doc.add(new LongField(FN_MODIFIED, System.currentTimeMillis(), Field.Store.YES));
        FieldType fType = new FieldType(StringField.TYPE_STORED);
        for (String tag : tags.toLowerCase().split(",")) {
            String trimTag = tag.trim();
            doc.add(new Field(FN_TAGS, trimTag, fType));
            textBuf.append(" " + trimTag + " ");
        }

        doc.add(new TextField(FN_TEXT, textBuf.toString(), Field.Store.NO));
        // update existing document if there
        writer.updateDocument(new Term(FN_URL, url), doc);
        writer.commit();
        smgr.maybeRefreshBlocking();
    }

    /**
     * Search Bookmarks
     * @param qterm    : filter by query
     * @param tags     : filter by tags
     * @param hitsPerPage
     * @return List of Links
     * @throws ParseException
     * @throws IOException
     */
    public static ArrayList<Link> searchBookmarks(String qterm, String tags, int hitsPerPage)
             throws ParseException, IOException {
        return searchBookmarks(qterm, tags, hitsPerPage, 0);
    }

    /**
     * Search Bookmarks with Offset
     * @param qterm      :Query
     * @param tags       : tags filter
     * @param hitsPerPage
     * @param offset     : start value used in pagination
     * @return     List of Links
     * @throws ParseException
     * @throws IOException
     */
    public static ArrayList<Link> searchBookmarks(String qterm, String tags, int hitsPerPage, int offset)
             throws ParseException, IOException {
        StringBuffer queryStr = new StringBuffer();
        if (qterm != null && !qterm.isEmpty()) {
            queryStr.append(qterm);
        }
        if (tags != null && !tags.isEmpty()) {
            if (queryStr.length() > 0) {
                queryStr.append(" AND ");
            }
            queryStr.append("tags:(");
            for (String tag : tags.split(",")) {
                queryStr.append("\"" + tag.trim() + "\" ");
            }
            queryStr.append(")");
        }
        Sort sort = null;
        if (queryStr.length() == 0) {
            queryStr.append("*:*");     // if no query present get all results
            SortField mod = new SortField(FN_MODIFIED, SortField.Type.LONG, true);
            sort = new Sort(mod);
        }

        Query q = new QueryParser(Version.LUCENE_45, FN_TEXT, analyzer).parse(queryStr.toString());
        logger.info("Executing QUERY: " + queryStr.toString());
        return searchBookmarks(q, hitsPerPage, offset, sort);
    }

    /**
     * Search bookmarks by lucene Query
     * @param q      : Lucene Query
     * @param count  : max documents to return
     * @param offset : start value used in pagination
     * @param sort   : sort field and order
     * @return   List of Bookmarks
     * @throws IOException
     */
    public static ArrayList<Link> searchBookmarks(Query q, int count, int offset, Sort sort) throws IOException {
        IndexSearcher searcher = smgr.acquire();
        TopDocs docs;
        if (sort != null) {
            docs = searcher.search(q, offset + count, sort);
        } else {
            docs = searcher.search(q, offset + count);
        }
        ScoreDoc[] hits = docs.scoreDocs;

        ArrayList<Link> res = new ArrayList<Link>();

        logger.info("Found " + docs.totalHits + " hits for QUERY: " + q.toString());
        for (int i = offset; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            String[] docTags = d.getValues(FN_TAGS);
            Date modified = new Date(new Long(d.get(FN_MODIFIED)));
            float score = hits[i].score;
            Link link = new Link(d.get(FN_URL), d.get(FN_TITLE), docTags, modified, score);
            res.add(link);
            logger.debug("\t" + i + ".\t" + link);
        }
        smgr.release(searcher);
        return res;
    }
}

