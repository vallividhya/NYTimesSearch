package com.codepath.nytimessearch.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by vidhya on 9/21/17.
 */

public class URLEncoder {

    //fq=news_desk:("Education"%20"Health")
    public static String encodeNewsDeskValues(boolean v1, boolean v2, boolean v3) {
        String param = "";
        StringBuilder sb = new StringBuilder();
        // Only if one of the news desk values are selected, construct query param, else return empty string
        if (v1 || v2 || v3) {
            sb.append("news_desk.contains:(");
            if (v1) {
                sb.append("\"Arts\"");
            }
            if (v2) {
                sb.append("\"Fashion\"");
            }
            if (v3) {
                sb.append("\"Sports\"");
            }
            sb.append(")");
        }

        Log.d("DEBUG", "===== News Desk Query String = " + sb.toString());
        try {
            param = java.net.URLEncoder.encode(sb.toString(),"UTF-8");
            Log.d("DEBUG", "===== News Desk Query String encoded= " + param);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return param;
    }

}
