package com.cloud.exam.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dyl on 2022/6/2.
 */
public class JSONtEST22 {

    public static void main(String[] args) {
        List<HashMap<String,Object>> ll = new ArrayList<>();
        HashMap<String,Object> map = new HashMap<>();
        map.put("id",5);
        ll.add(map);
        System.out.println(JSON.toJSONString(ll));
    }
}
