package org.simplyphi.pbms.api;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.simplyphi.pbms.lucene.LuceneIndex;
import org.simplyphi.pbms.models.Link;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Root resource (exposed at "pbms" path)
 */
@Path("link")
public class LinksAPI {
    private static final Logger logger = Logger.getLogger(LinksAPI.class);

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "Hello!";
    }

    /**
     * This Method handles HTTP POST requests on 'link/delete' end point
     * Deletes documents matching to supplied arguments.
     * @param url      : Deletes Link with this Url value
     * @param tags     : Deletes Links matching to Tags value
     * @return
     */
    @POST
    @Path("delete")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_HTML)
    public String deleteBookmarksPost(
            @DefaultValue("") @FormParam("url") String url,
            @DefaultValue("") @FormParam("tags") String tags ) {
        logger.debug("POST : /link/delete ::  url=" + url + " , tags="+ tags);
        return deleteBookmarks(url, tags);
    }

    /**
     * This Method handles HTTP DELETE request on /link end point
     * Deletes documents matching to supplied arguments
     * @param url    : deletes link whose url is matching to value of url
     * @param tags   : deletes all links which have any of tags matching to value of this
     * @return
     */
    @DELETE
    @Produces(MediaType.TEXT_HTML)
    public String deleteBookmarks(
            @DefaultValue("") @FormParam("url") String url,
            @DefaultValue("") @FormParam("tags") String tags) {

        logger.debug("DELETE : /link ::  url=" + url + " , tags="+ tags);
        try {
            if(!(url == null || url.isEmpty())){
                LuceneIndex.deleteBookmark(url);
                logger.info("Deleted bookmarks with url : " + url);
            }
            if(!(tags == null || tags.isEmpty())){
                for(String tag : tags.split(",")){
                    LuceneIndex.deleteBookmarkByTag(tag.trim());
                    logger.info("Deleted bookmarks with tag : " + tag);
                }
            }
        } catch (IOException e) {
            logger.error("IOException in Delete: ", e);
        }
        return String.format("<p> bookmarks with url: %s and tags: %s deleted</p>" ,
                url, Arrays.asList(tags.split(",")));
    }

    /**
     * This Method handles HTTP POST on /link  end point
     * adds a new link with supplied arguments into Lucene index
     * @param url   : value of URI of page
     * @param title : Title of page
     * @param tags  : Tags separated by comma
     * @return
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes("application/x-www-form-urlencoded")
    public String addBookmark(
            @DefaultValue("") @FormParam("url") String url,
            @DefaultValue("") @FormParam("title") String title,
            @DefaultValue("") @FormParam("tags") String tags ) {

        logger.debug("POST : /link :: { url:" + url + ", title:" + title+ " , tags:"+ tags + "}");
        try {
            if( title.isEmpty()) {
                //If title is missing copy url to title
                title = url;
            }
            LuceneIndex.addBookmark(title, url, tags);
            logger.info(String.format("Add Bookmark: FN_URL=%s\tTitle=%s\tTags=%s", url, title, tags));
        } catch (IOException e) {
            logger.error("IOException in AddBookmark: ", e);
        }
        return "";
    }

    /**
     * This Method handles HTTP GET on "/link" end point
     * This method returns Link documents matching to supplied filters
     * @param query : Query
     * @param tags  : tags
     * @param offset : start value used in pagination
     * @param count  :number 0f documents to return at max
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Link> searchBookmarks(
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("") @QueryParam("tags") String tags,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("10") @QueryParam("count") int count ) {

        logger.info("GET : /link :: { q:" + query + ", tags:" + tags + " , offset:"+ offset + ", count:" + count + "}");
        try {
            return LuceneIndex.searchBookmarks(query, tags, count, offset);
        } catch (ParseException e) {
            logger.error("Parse Exception in Search", e);
        } catch (IOException e) {
            logger.error("IOException in Search", e);
        }
        return null;
    }


    /**
     * This Method handles HTTP GET on "/link" end point
     * This method searches for Link documents matching to supplied filters and returns minimal HTML view of documents
     * @param query : Query
     * @param tags  : tags
     * @param offset : start value used in pagination
     * @param count  :number 0f documents to return at max
     * @return
     */
    @GET
    @Path("html")
    @Produces(MediaType.TEXT_HTML)
    public String searchBookmarksHTML(
            @DefaultValue("") @QueryParam("q") String query,
            @DefaultValue("") @QueryParam("tags") String tags,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("10") @QueryParam("count") int count ) {

        logger.debug("GET : /link/html :: { q:" + query + ", tags:" + tags + " , offset:"+ offset + ", count:" + count + "}");
        StringBuffer buf = new StringBuffer();
        buf.append("<div class=\"searchResults\" id=\"searchresults\">\n" +
                "\t<ol>\n");
        try {
            ArrayList<Link> links = LuceneIndex.searchBookmarks(query, tags, count, offset);
            for (Link l : links) {
                buf.append("\t\t<li>\n\t\t\t<div class=\"result\">\n");
                buf.append("\t\t\t<span><form action=\"/pbms/link/delete\" method=\"POST\">\n" +
                        "\t\t\t\t<input type=\"hidden\" name=\"url\" id=\"url\" style=\"width:75%\" value=\"" +
                                l.getUrl() +"\"/>\n");
                buf.append("\t\t\t\t<input type=\"submit\" name=\"add\" value=\"Del\" />\n\t\t\t</form>");
                buf.append("\t\t\t\t<a href=\"" + l.getUrl() + "\" class=\"resultLink\" target=\"_blank\">" +
                        "<span class=\"linkTitle\">" + l.getTitle() +
                        "</span> </a> <small class=\"tags\">");
                for (String tag : l.getTags()) {
                    buf.append("<a href=\"/pbms/link/html?tags=" + tag + "\">" +
                            tag + "</a>, ");
                }
                buf.delete(buf.lastIndexOf(", "), buf.length());
                buf.append(
                        "</small>\n\t\t\t</div>\n\t\t</li></span>\n\n");
            }

        } catch (ParseException e) {
            logger.error("Parse Exception in Search HTML", e);
        } catch (IOException e) {
            logger.error("IOException in Search HTML", e);
        }
        buf.append("\t</ol>\n</div>");
        return buf.toString();
    }

}
