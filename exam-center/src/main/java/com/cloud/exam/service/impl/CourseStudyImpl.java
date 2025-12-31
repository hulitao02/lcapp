package com.cloud.exam.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.*;
import com.cloud.exam.model.course.*;
import com.cloud.exam.service.CourseStudyService;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class CourseStudyImpl extends ServiceImpl<CourseStudyDao,CourseStudy> implements CourseStudyService {

    @Autowired
    private CourseStudyDao courseStudyDao;
    @Autowired
    private CourseUserRelDao courseUserRelDao;
    @Autowired
    private CourseQuestionRelDao courseQuestionRelDao;
    @Autowired
    private CourseKpRelDao courseKpRelDao;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager ;
    // 数据库标识 1：达梦数据库 2：pg数据库
    @Value(value = "${db-type}")
    private Integer dbType;

    @Override
    public List<CourseStudy> findCourseByStudentId(Date d1, Date d2, Long id) {
        if (dbType==1){
            return courseStudyDao.findCourseByStudentIdDM(d1,d2,id);
        }
        return courseStudyDao.findCourseByStudentId(d1,d2,id);
    }

    @Override
    public CourseStudy copyCourse(CourseStudy courseStudyDetail ,Long id) {

        CourseStudy c = new CourseStudy() ;

        final  boolean b ;
        try {

            Long courseId = courseStudyDetail.getId() ;
            List<TransactionStatus> list1 = new ArrayList<>() ;
            DefaultTransactionDefinition def = new DefaultTransactionDefinition() ;
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            //开启的线程数
            AtomicInteger totalThread = new AtomicInteger(3) ;
            AtomicBoolean isException = new AtomicBoolean(Boolean.FALSE) ;

            List<Thread> unFinishedThread = Collections.synchronizedList(new ArrayList<>()) ;
            List<CompletableFuture<Boolean>>  list = new ArrayList<>() ;

/*
            CompletableFuture<Long> c0 = CompletableFuture.supplyAsync(()->{

                String name = Thread.currentThread().getName();
                System.out.println(name+"===c0");
                //获得 事务状态
                TransactionStatus status = dataSourceTransactionManager.getTransaction(def) ;
                CourseStudy courseStudy = new CourseStudy() ;
                try {
                    Thread.currentThread().setName("coursestudy");
                    list1.add(status) ;
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                    BeanUtils.copyProperties(courseStudyDetail,courseStudy,"id","createTime");
                    courseStudy.setCreateTime(new Date());
                    courseStudyDao.insert(courseStudy);
                    //totalThread.decrementAndGet();
                    unFinishedThread.add(Thread.currentThread()) ;
                    this.notifyAllThread(unFinishedThread,totalThread,false);
                    LockSupport.park();
                    if(isException.get()){
                        System.out.println("回滚");
                        dataSourceTransactionManager.rollback(status);
                    }else {
                        System.out.println("提交");
                        dataSourceTransactionManager.commit(status);
                    }

                }catch (Exception e){
                    System.out.println("新建courseStydy错了、、、、、、、、、、");
                    isException.set(Boolean.TRUE);
                    e.printStackTrace();
                    dataSourceTransactionManager.rollback(status);
                    this.notifyAllThread(unFinishedThread,totalThread,true);
                    throw new RuntimeException("提交courseStydy出错了mmmmmm");
                }
                    return courseStudy.getId() ;
                });
                list.add(c0) ;*/
                CompletableFuture<Boolean> c1 = CompletableFuture.supplyAsync(()->{
                    String name = Thread.currentThread().getName();
                    System.out.println(name+"===c1");
                    //获得 事务状态
                    TransactionStatus status = dataSourceTransactionManager.getTransaction(def) ;
                    try {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                        QueryWrapper<CourseQuestionRel> questionRelQueryWrapper = new QueryWrapper<>();
                        questionRelQueryWrapper.eq("course_id",courseId);
                        List<CourseQuestionRel> courseQuestionRels = courseQuestionRelDao.selectList(questionRelQueryWrapper);
                        courseQuestionRels.stream().forEach(e->{
                            CourseQuestionRel courseQuestionRel = new CourseQuestionRel();
                            BeanUtils.copyProperties(e,courseQuestionRel,"id","courseId");
                            courseQuestionRel.setCourseId(id);
                            courseQuestionRelDao.insert(courseQuestionRel) ;
                        });
                        //totalThread.decrementAndGet();
                        unFinishedThread.add(Thread.currentThread()) ;
                        this.notifyAllThread(unFinishedThread,totalThread,false);
                        LockSupport.park();
                        if(isException.get()){
                            System.out.println("回滚1");
                            dataSourceTransactionManager.rollback(status);
                        }else {
                            System.out.println("提交1");
                            dataSourceTransactionManager.commit(status);
                        }
                        return true ;
                    }catch (Exception e){
                        c.setName("测试");
                        System.out.println("新建CourseQuestionRel错了、、、、、、、、、、");
                        isException.set(Boolean.TRUE);
                        e.printStackTrace();
                        dataSourceTransactionManager.rollback(status);
                        this.notifyAllThread(unFinishedThread,totalThread,true);
                        //throw new RuntimeException("提交1出错了mmmmmm");
                        return false ;
                    }
                });
                list.add(c1) ;
                CompletableFuture<Boolean> c2 = CompletableFuture.supplyAsync(()->{
                    String name = Thread.currentThread().getName();
                    System.out.println(name+"===c2");
                    //获得 事务状态
                    TransactionStatus status = dataSourceTransactionManager.getTransaction(def);
                    list1.add(status) ;
                    try {
                        QueryWrapper<CourseKpRel> kpRelQueryWrapper = new QueryWrapper<>();
                        kpRelQueryWrapper.eq("course_id", courseId);
                        List<CourseKpRel> courseKpRels = courseKpRelDao.selectList(kpRelQueryWrapper);
                        courseKpRels.stream().forEach(e -> {
                            CourseKpRel courseKpRel = new CourseKpRel();
                            BeanUtils.copyProperties(e, courseKpRel, "id", "courseId");
                            courseKpRel.setCourseId(id);
                            courseKpRelDao.insert(courseKpRel);
                        });
                        //totalThread.decrementAndGet();
                        unFinishedThread.add(Thread.currentThread());
                        this.notifyAllThread(unFinishedThread, totalThread, false);
                        LockSupport.park();
                        if(isException.get()){
                            System.out.println("回滚2");
                            dataSourceTransactionManager.rollback(status);
                        }else {
                            System.out.println("提交2");
                            dataSourceTransactionManager.commit(status);
                        }
                        return true ;
                    }catch (Exception e){
                        System.out.println("新建CourseKpRel错了、、、、、、、、、、");
                        isException.set(Boolean.TRUE);
                        e.printStackTrace();
                        dataSourceTransactionManager.rollback(status);
                        this.notifyAllThread(unFinishedThread,totalThread,true);
                        //throw new RuntimeException("提交2出错了mmmmmm");
                        return false ;
                    }
                });
                list.add(c2) ;
                CompletableFuture<Boolean> c3 = CompletableFuture.supplyAsync(()-> {
                    String name = Thread.currentThread().getName();
                    System.out.println(name+"===c3");
                    //获得 事务状态
                    TransactionStatus status = dataSourceTransactionManager.getTransaction(def);
                    list1.add(status) ;
                    try {
                        CourseStudy courseStudy = courseStudyDao.selectById(1000L);
                        System.out.println(courseStudy.getName());
                        QueryWrapper<CourseUserRel> userRelQueryWrapper = new QueryWrapper<>();
                        userRelQueryWrapper.eq("course_id", courseId);
                        List<CourseUserRel> courseUserRels = courseUserRelDao.selectList(userRelQueryWrapper);
                        courseUserRels.stream().forEach(e -> {
                            CourseUserRel courseUserRel = new CourseUserRel();
                            BeanUtils.copyProperties(e, courseUserRel, "id", "courseId");
                            courseUserRel.setCourseId(id);
                            courseUserRelDao.insert(courseUserRel);
                        });
                        //totalThread.decrementAndGet();
                        unFinishedThread.add(Thread.currentThread());
                        this.notifyAllThread(unFinishedThread, totalThread, false);
                        LockSupport.park();
                        if(isException.get()){
                            System.out.println("回滚3");
                            dataSourceTransactionManager.rollback(status);
                        }else {
                            System.out.println("提交3");
                            dataSourceTransactionManager.commit(status);
                        }
                    }catch (Exception e){
                        c.setName("测试3");
                        isException.set(Boolean.TRUE);
                        System.out.println("出错了。。。。。。。。。。。。。。。。");
                        e.printStackTrace();
                        dataSourceTransactionManager.rollback(status);
                        this.notifyAllThread(unFinishedThread,totalThread,true);
                        throw new RuntimeException("提交3出错了mmmmmm");
                    }
                    return true ;
                });
                list.add(c3) ;
                CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));

                System.out.println("各个子线程完成了。。。。。。。。。。。。。。。。");


               /* if(isException.get()){
                    System.out.println("最后判断。。。。。。。。。。。。。。。。"+isException.get());
                    list1.stream().forEach(e->{
                        dataSourceTransactionManager.rollback(e);
                    });
                }*/


        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println("出错了。。。。。。。。。。。。。。。。111111");
            throw  new RuntimeException("异常了。。。。。");
        }

        return c;
    }

    private void notifyAllThread(List<Thread> unfinishedList,AtomicInteger ai ,boolean isForce){
        System.out.println(Thread.currentThread().getName()+"========"+unfinishedList.size()+"===="+ai.get());
        if(isForce || unfinishedList.size() == ai.get()){
            for(Thread thread : unfinishedList){
                LockSupport.unpark(thread);
            }
        }
    }

    @Autowired
    CourseKpDao courseKpDao;
    @Override
    @Transactional
    public void copyCourse1(CourseStudy courseStudyDetail) {
        Long courseId = courseStudyDetail.getId() ;

        CourseStudy courseStudy = new CourseStudy();
        BeanUtils.copyProperties(courseStudyDetail, courseStudy, "id", "createTime");
        courseStudy.setCreateTime(new Date());
        courseStudyDao.insert(courseStudy);
        Long id = courseStudy.getId() ;
        QueryWrapper<CourseQuestionRel> questionRelQueryWrapper = new QueryWrapper<>();
        questionRelQueryWrapper.eq("course_id",courseId);
        List<CourseQuestionRel> courseQuestionRels = courseQuestionRelDao.selectList(questionRelQueryWrapper);
        courseQuestionRels.stream().forEach(e->{
            CourseQuestionRel courseQuestionRel = new CourseQuestionRel();
            BeanUtils.copyProperties(e,courseQuestionRel,"id","courseId");
            courseQuestionRel.setCourseId(id);
            courseQuestionRelDao.insert(courseQuestionRel) ;
        });

        // 知识点复制逻辑
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("course_id",courseId);
        List<CourseKp> list = courseKpDao.selectList(queryWrapper);
        for (CourseKp courseKp:list){
            CourseKp bean=new CourseKp();
            bean.setCourseId(id);
            bean.setKpId(courseKp.getKpId());
            courseKpDao.insert(bean);
        }
        QueryWrapper<CourseKpRel> kpRelQueryWrapper = new QueryWrapper<>();
        kpRelQueryWrapper.eq("course_id", courseId);
        List<CourseKpRel> courseKpRels = courseKpRelDao.selectList(kpRelQueryWrapper);
        courseKpRels.stream().forEach(e -> {
            CourseKpRel courseKpRel = new CourseKpRel();
            BeanUtils.copyProperties(e, courseKpRel, "id", "courseId");
            courseKpRel.setCourseId(id);
            courseKpRelDao.insert(courseKpRel);
        });
        QueryWrapper<CourseUserRel> userRelQueryWrapper = new QueryWrapper<>();
        userRelQueryWrapper.eq("course_id", courseId);
        List<CourseUserRel> courseUserRels = courseUserRelDao.selectList(userRelQueryWrapper);
        courseUserRels.stream().forEach(e -> {
            CourseUserRel courseUserRel = new CourseUserRel();
            BeanUtils.copyProperties(e, courseUserRel, "id", "courseId");
            courseUserRel.setCourseId(id);
            courseUserRelDao.insert(courseUserRel);
        });
    }
}
