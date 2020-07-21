package ca.gc.aafc.objectstore.api.entities;

import java.util.HashMap;


public class License {
    private String name;
    private String url;
    private HashMap<language, String> titles;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }

    public HashMap<language, String> getTitles() { return titles; }

    public void setTitles(HashMap<language, String> titles) { this.titles = titles; }

    public enum language {
        en,
        fr
    }
}
