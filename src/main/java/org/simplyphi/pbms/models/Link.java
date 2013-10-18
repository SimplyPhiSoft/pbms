package org.simplyphi.pbms.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: umar
 * Date: 30/9/13
 * Time: 2:21 PM
 */
@XmlRootElement(name="item")
@XmlType
public class Link {
    private String title;
    private String url;
    private String tags[];
    private Date createdAt;
    private float score;

    public Link(){
    }

    public Link(String url, String title, String[] tags,  Date createdAt, float score){
        this.url = url;
        this.title = title;
        this.tags = tags;
        this.createdAt = createdAt;
        this.score = score;
    }

    @XmlElement(name="title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement(name="url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement(name="date")
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @XmlElement(name="score")
    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @XmlElement(name="tags")
    public String[] getTags() {
        return tags;
    }

    public void setTags(String tags[]) {
        this.tags = tags;
    }

    public String toString(){
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(String.format("\t%s\t%s\t%s\t%f",
                url, title, createdAt, score));
        sbuf.append("\n\t\t\tTags: " + Arrays.asList(tags));
        return sbuf.toString();

    }
}