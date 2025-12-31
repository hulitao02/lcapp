package com.cloud.user.testmain;

import com.alibaba.fastjson.JSON;
import com.cloud.utils.CollectionsCustomer;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class TestParse<K extends Object, V extends Object> {


    private static TestParse.Builder builder() {
        return new TestParse.Builder();
    }

    public TestParse(TestParse.Builder builder) {

    }


    public static class Builder {
        public TestParse build() {
            return new TestParse(this);
        }
    }


    /**
     * 把KEY值转成小写。
     * ""
     *
     * @param sourceList
     * @return
     */
    public List<Map<K, V>> listMapToLowerCase(List<Map<? extends K, ? extends V>> sourceList) {
        if (!CollectionUtils.isEmpty(sourceList)) {
            List<Map<K, V>> ListNew = new ArrayList<>();
            sourceList.stream().forEach(map -> {
                Map<K, V> newMap = new HashMap();
                map.forEach((k, v) -> {
                    if (k instanceof String) {
                        newMap.put((K) k.toString().toLowerCase(), v);
                    }
                });
                ListNew.add(newMap);
            });
            return ListNew;
        }
        return Collections.emptyList();
    }


    /**
     * 都转成小写KEY
     *
     * @return
     */
    public Map<K, V> mapToLowerCase(Map<? extends K, ? extends V> sourceMap) {
        if (!CollectionUtils.isEmpty(sourceMap)) {
            Map<K, V> newMap = new HashMap();
            sourceMap.forEach((k, v) -> {
                newMap.put((K) k.toString().toLowerCase(), v);
            });
            return newMap;
        }
        return (Map<K, V>) sourceMap;
    }


    public static void main(String[] args) {


        Map map = new HashMap();
        map.put("ID", 11);
        map.put("USER_NAME", "名称");
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(map);


        System.out.println(JSON.toJSONString(TestParse.builder().build().listMapToLowerCase(list)));


    }


}
