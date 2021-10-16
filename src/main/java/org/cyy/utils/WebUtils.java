package org.cyy.utils;

import java.io.*;
import java.util.Properties;


public class WebUtils {
    public static boolean groupIsOk(long id,int operation){
        File file = new File("group/"+id+".properties");
        if(file.exists()){
            return true;
        }else if(operation == 1){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }else{
            return false;
        }
    }
    public static boolean collegeIsOk(String college,int operation){
        File file = new File("college/"+college+".properties");
        if(file.exists()){
            return true;
        }else if(operation == 1){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }else{
            return false;
        }
    }
    public static void closeResource(InputStream in,OutputStream out){
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static Properties loadProperties(String path){
        InputStream is = null;
        Properties properties = null;
        try {
            is = new FileInputStream(path);
            properties = new Properties();
            properties.load(is);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            WebUtils.closeResource(is,null);
        }
        return properties;
    }
    public static void closeResource(BufferedReader in,OutputStreamWriter out){
        if(in != null){
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(out != null){
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
