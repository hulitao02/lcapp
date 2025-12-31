package com.cloud.exam.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.*;


public class ReadJsonUtils {
	
	/**
     * 读取json文件返回数据
     * @throws IOException
     */
    public static String readJsonData(String filePath){
        String jsonString = "";
        Reader reader = null;
        try{
            ClassPathResource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                return null;
            }
            File file = resource.getFile();
            reader = new InputStreamReader(new FileInputStream(file),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
            jsonString = sb.toString();
        } catch (IOException e){
            return "";
        }
        return jsonString;

    }


}
