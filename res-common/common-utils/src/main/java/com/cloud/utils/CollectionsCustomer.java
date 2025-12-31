package com.cloud.utils;


import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * List<Map>
 * 达梦数据库返回的KEY值都是大写
 * ,都转成小写
 */
public class CollectionsCustomer<K,V>  {


    public static Builder builder(){
        return new Builder();
    }

    private CollectionsCustomer(Builder builder) {
    }

//    内部类
    public static class Builder{
        public CollectionsCustomer build() {
            return new CollectionsCustomer(this);
        }
    }


    /**
     * 把KEY值转成小写。
     * ""
     * @param sourceMapList
     * @return
     */
    public List<Map<K, V>> listMapToLowerCase(List<Map<? extends K,? extends V>> sourceMapList) {
        if (!CollectionUtils.isEmpty(sourceMapList)) {
            List<Map<K,V>> ListNew = new ArrayList<>();
            sourceMapList.stream().forEach(map -> {
                Map<K,V> newMap = new HashMap();
                map.forEach((k, v) -> {
                    newMap.put((K)k.toString().toLowerCase(), v);
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
    public Map<K,V> mapToLowerCase(Map<? extends K,? extends V> sourceMap) {
        if(!CollectionUtils.isEmpty(sourceMap)){
            Map<K,V> newMap = new HashMap();
            sourceMap.forEach((k, v) -> {
                newMap.put((K)k.toString().toLowerCase(), v);
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

        CollectionsCustomer.builder().build().listMapToLowerCase(list);

    }


}
