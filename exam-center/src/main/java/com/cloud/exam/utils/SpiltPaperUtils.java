package com.cloud.exam.utils;

import java.util.*;

/**
 * Created by dyl on 2021/03/25.
 * 向考试人员分配试卷 座位
 */
public class SpiltPaperUtils {

    public static void main(String[] args) {
        List<Integer> l1 = new ArrayList<>();
        for(Integer i = 0;i<5;i++){
            l1.add(i);
        }
        List<Integer> l2 = new ArrayList<>();
        for(Integer i = 0;i<16;i++){
            l2.add(i);
        }

       //System.out.println(SpiltPaper(l2,l1));
    }
    //分配试卷 {试卷id,人员id}
    public static Map<Long,Long> SpiltPaper(List<Long> userIds, List<Long> paperIds) {
        //打乱list顺序,这样避免余数每次都分配到第一个人上面
        Collections.shuffle(userIds);
        Collections.shuffle(paperIds);
        //待分配的人数
        Integer userCount = userIds.size();
        //总试卷数
        Integer paperCount = paperIds.size();
        //余数
        Integer remainderCount = userCount % paperCount;
        //每个试卷分配人数
        Integer divideCount = userCount / paperCount;
        Map<Long,Long> map1 = new HashMap<>();//保存人员--试卷
        for (int i = 0; i < paperCount; i++) {
            for (int j = 0; j <= divideCount; j++) {
                if (paperCount * (j) + i <= userIds.size() - 1) {
                    map1.put(userIds.get(paperCount * (j) + i),paperIds.get(i));
                }
            }
        }
        return map1;
    }

    /**
     * 竞答活动只抽取人员和座位
     * @param placesIds
     * @return
     */
    public static Map<Long,String> spiltplace(Integer unit, Map<Long, List<Long>> map2,List<String> placesIds){
        Map<Long,String> map1 = new HashMap<>();
        List<Long> userIds = new ArrayList<>();
        for (Long departId:map2.keySet()) {
            map2.get(departId).stream().forEach(l->userIds.add(l));
        }
        int totalCount = placesIds.size();
        if(0==unit){
            //团体
            for (Long departId:map2.keySet()) {
                Random random = new Random();
                int i = random.nextInt(totalCount);
                String s = placesIds.get(i);
                placesIds.remove(s);
                totalCount--;
                for (Long userId:map2.get(departId)) {
                    map1.put(userId,s);
                }
            }
        }else if(1==unit){
            //个人
            Collections.shuffle(userIds);
            for (Long userId:userIds) {
                Random random = new Random();
                int i = random.nextInt(totalCount);
                String s = placesIds.get(i);
                placesIds.remove(s);
                totalCount--;
                map1.put(userId,s);
            }
        }
        return map1;
    }
}
