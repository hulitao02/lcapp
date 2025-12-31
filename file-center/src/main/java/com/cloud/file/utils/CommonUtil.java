package com.cloud.file.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

/**
 * @author:胡立涛
 * @description: TODO http Jsoup get,post请求
 * @date: 2022/4/22
 * @param:
 * @return:
 */
public class CommonUtil {


    /**
     * @author:胡立涛
     * @description: TODO http post请求
     * @date: 2022/4/21
     * @param: [url, json]
     * @return: com.alibaba.fastjson.JSONObject
     */
    public static JSONObject esMethod(String url, JSONObject json) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .ignoreContentType(true)
                    .requestBody(json.toString())
                    .method(Connection.Method.POST)
                    .timeout(9000)
                    .execute();
            String body = response.body();
            JSONObject result = JSON.parseObject(body);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @author:胡立涛
     * @description: TODO http get请求
     * @date: 2022/4/21
     * @param: [url, param]
     * @return: com.alibaba.fastjson.JSONObject
     */
    public static JSONObject esGetMethod(String url, String param) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .ignoreContentType(true)
                    .method(Connection.Method.GET)
                    .data("path", param)
                    .timeout(9000)
                    .execute();
            String body = response.body();
            JSONObject result = JSON.parseObject(body);
            return result;
        } catch (Exception e) {
            try {
                Connection.Response response = Jsoup.connect(url)
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .ignoreContentType(true)
                        .method(Connection.Method.GET)
                        .data("path", param)
                        .timeout(9000)
                        .execute();
                String body = response.body();
                JSONObject result = JSON.parseObject(body);
                return result;
            } catch (Exception e1) {
                e1.printStackTrace();
                return null;
            }
        }
    }
}
