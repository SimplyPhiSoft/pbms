package org.simplyphi.pbms;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import junit.framework.Assert;
import org.apache.lucene.queryparser.classic.ParseException;
import org.glassfish.grizzly.http.server.HttpServer;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simplyphi.pbms.lucene.LuceneIndex;
import org.simplyphi.pbms.models.Link;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class LinksAPITest {

    private HttpServer server;
    private WebTarget target;
    private final int hitsPerPage = 10;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newBuilder().register(JacksonFeature.class).build();

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and Main.startServer())
        // --
        // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

        target = c.target(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void pingTest() {
        String responseMsg = target.path("link/ping").request().get(String.class);
        assertEquals("Hello!", responseMsg);
    }
    /**
     * Test to see that the link is getting indexed
     */
    @Test
    public void testAddAndSearch() throws IOException, ParseException {
        String title = "SMXFGTT WRD564 XXFFSS223344";
        String url = "http://test.domain.in/SMXFGTT-WRD564-XXFFSS223344";
        String tags = "SMXFGTT, WRD564 , XXFFSS223344";
        LuceneIndex.addBookmark(title, url, tags);
        ArrayList<Link> links = LuceneIndex.searchBookmarks("SMXFGTT XXFFSS223344", "", hitsPerPage);
        Assert.assertEquals(1, links.size());
        Assert.assertEquals(title, links.get(0).getTitle());
        Assert.assertEquals(url, links.get(0).getUrl());
        LuceneIndex.deleteBookmark(url);
        links = LuceneIndex.searchBookmarks("SMXFGTT XXFFSS223344", "", hitsPerPage);
        Assert.assertEquals(0, links.size());
    }

    @Test
    public void testAddAndDeleteByTags() throws IOException, ParseException {
        String title = "SMXFGTT WRD564 XXFFSS223344";
        String url = "http://test.domain.in/SMXFGTT-WRD564-XXFFSS223344";
        String tags = "SMXFGTT, WRD564 , XXFFSS223344";
        LuceneIndex.addBookmark(title, url, tags);
        ArrayList<Link> links = LuceneIndex.searchBookmarks("WRD564 XXFFSS223344", "", hitsPerPage);
        Assert.assertEquals(1, links.size());
        Assert.assertEquals(title, links.get(0).getTitle());
        Assert.assertEquals(url, links.get(0).getUrl());
        LuceneIndex.deleteBookmarkByTag(tags.split(",")[0]);
        links = LuceneIndex.searchBookmarks("SMXFGTT XXFFSS223344", "", hitsPerPage);
        Assert.assertEquals(0, links.size());
    }
}
