package com.cloud.exam.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.CourseUserRelDao;
import com.cloud.exam.model.course.CourseUserRel;
import com.cloud.exam.service.CourseUserRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class CourseUserRelImpl extends ServiceImpl<CourseUserRelDao,CourseUserRel> implements CourseUserRelService {

    @Autowired
    private CourseUserRelDao courseUserRelDao;


    @Override
    public List<Integer>
    getPerMonthCountByUser(String paramsDate, Long userId) {
        List<Integer> ll = new ArrayList<>();
        Map<String,Integer> map = MapUtil.newHashMap(true);
        for (int i = 1; i < 13 ; i++) {
            map.put(i+"",0);
        }
        List<Map<String, Integer>> perMonthCountByUser = courseUserRelDao.getPerMonthCountByUser(paramsDate, userId);
        //Map<String,Integer> mm = new LinkedHashMap<>();
        perMonthCountByUser.stream().forEach(e->{
            map.put(e.get("date")+"",e.get("count"));
        });
        //map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(ee->mm.put(ee.getKey(),ee.getValue()));
        map.entrySet().stream().forEach(ee->ll.add(ee.getValue()));
        return ll;
    }
}
