package org.cyy.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class MyConnection{
    private HttpURLConnection connection;

    public MyConnection setUrl(String url) throws Exception {
        URL realUrl = new URL(url);
        this.connection = (HttpURLConnection) realUrl.openConnection();
        return this;
    }

    public MyConnection setCookie(String cookie){
        if(connection != null){
            connection.setRequestProperty("cookie", cookie);
            //connection.setInstanceFollowRedirects(false);
        }
        return this;
    }

    public MyConnection set302(boolean judge){
        if(connection != null){
            connection.setInstanceFollowRedirects(judge);
        }
        return this;
    }

    public MyConnection setMethod(String method) throws Exception {
        if(connection != null){
            connection.setRequestMethod("POST");
        }
        return this;
    }

    public MyConnection setInput(boolean judge){
        if(connection != null){
            connection.setDoInput(judge);
        }
        return this;
    }
    public  MyConnection setOutput(boolean judge){
        if(connection != null){
            connection.setDoOutput(judge);
        }
        return this;
    }

    public MyConnection setAuthorization(String authorization){
        if(connection != null){
            connection.setRequestProperty("authorization",authorization);
        }
        return this;
    }
    public HttpURLConnection build(){
        return connection;
    }
}
