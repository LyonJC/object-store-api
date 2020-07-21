package ca.gc.aafc.objectstore.api.entities;

import java.util.HashMap;


public class License {
    private String url;
    private HashMap<String, String> titles;

    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }

    public HashMap<String, String> getTitles() { return titles; }

    public void setTitles(HashMap<String, String> titles) { this.titles = titles; }
}
