package com.codepath.nytimessearch.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by vidhya on 9/19/17.
 */

public class Article implements Serializable{

    String webUrl;
    String headLine;
    String thumbNail;

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getHeadLine() {
        return headLine;
    }

    public void setHeadLine(String headLine) {
        this.headLine = headLine;
    }

    public String getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(String thumbNail) {
        this.thumbNail = thumbNail;
    }



    public Article(JSONObject jsonObject) {
        try {
            this.webUrl = jsonObject.getString("web_url");
            this.headLine = jsonObject.getJSONObject("headline").getString("main");
            JSONArray jsonArray =  jsonObject.getJSONArray("multimedia");
            if (jsonArray.length() > 0) {
                JSONObject multimediaJson = jsonArray.getJSONObject(0);
                this.thumbNail = String.format("http://www.nytimes.com/%s", multimediaJson.getString("url"));
            } else {
                this.thumbNail = "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Article> fromJSONArray(JSONArray jArray) {
        ArrayList<Article> articlesList = new ArrayList<Article>();
        for (int i = 0; i < jArray.length(); i++) {
            try {
                articlesList.add(new Article(jArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return articlesList;
    }

}
