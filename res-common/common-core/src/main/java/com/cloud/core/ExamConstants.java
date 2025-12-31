package com.cloud.core;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dyl on 2021/04/09.
 * 考试活动常量
 */
public  class ExamConstants {

    public static final String ID = "id";
    public static final String ANSWER = "answer";
    public static final String PAPERID = "paperid";
    public static final List<Integer> TYPELIST = Arrays.asList(1,2,3,4);
    public static  Integer WIDTH = 1600;
    public static  Integer HEIGHT = 900;

    public static long EXT_TIME = 7*24*60*60*1000;
    /**
     * 活动状态
     */

    //待启动
    public static final Integer ACTIVITY_NOT_LAUNCH=0;
    //已启动
    public static final Integer ACTIVITY_LAUNCH=1;
    //活动开始--到达活动开始时间 （已废除，活动时间一到，即改为考试开始状态）
    public static final Integer ACTIVITY_START=2;
    //待判卷
    public static final Integer ACTIVITY_WAIT_JUDGE=3;
    //活动完成
    public static final Integer ACTIVITY_FINISH=4;
    //活动取消
    public static final Integer ACTIVITY_CONCELL=5;
    //考试开始--由管理员手动点击开始按钮
    public static final Integer ACTIVITY_EXAM_START=6;

    /**
     * 活动类型
     */
    public static final Integer EXAM_TYPE_LILUN=0;
    public static final Integer EXAM_TYPE_JINGDA=1;
    public static final Integer EXAM_TYPE_XUNLIAN=2;
    public static final Integer EXAM_TYPE_ZICE=3;
    /**
     * 学员考试状态
     */

    //未登录
    public static final Integer EXAM_NOT_LOGIN=0;
    //已登录
    public static final Integer EXAM_YES_LOGIN=1;
    //考试结束待判卷
    public static final Integer EXAM_WAIT_JUDGE=3;
    //判卷完成-> 考试完成
    public static final Integer EXAM_FINISH=4;
    //活动取消-> 考试取消
    public static final Integer EXAM_CONCELL=5;
    //考试开始,管理员点击考试开始
    public static final Integer EXAM_START=6;
    // 强制交卷
    public static final Integer EXAM_FORCE_FINISH=7;

    /**
     * redis存储活动相关记录的时间设置
     */
    //存储考生考试记录（试题答案）
    public static  final Long EXAM_STUDENT_ANSWER_DETAILS_TIME=7*60*60*12L;
    //存储活动试卷关联信息
    public static  final Long EXAM_PAPER_QUESTION_DETAILS_TIME=7*60*60*12L;
    //存储竞答活动试卷关联信息
    public static  final Long EXAM_COMPETITION_DETAILS_TIME=7*60*60*12L;
    //存储活动抽签信息
    public static  final Long EXAM_DRAWRESULT_DETAILS_TIME=7*60*60*12L;
    //存储其他信息
    public static  final Long EXAM_OTHER_DETAILS_TIME1=1*60*60*12L;
    public static  final Long EXAM_OTHER_DETAILS_TIME7=7*60*60*12L;
    /**
     * 用户角色
     */

    //超级管理员
    public static final Integer SYS_SUPER_ADMIN=1;
    public static final String SYSTEM_SUPER_ADMIN="SUPER_ADMIN";
    public static final String SYSTEM_TEACHER="teacher";
    //教员 ->具有创建活动权限
    public static final Integer SYS_TEACHER=2;
    //学员
    public static final Integer SYS_STUDENT=3;
    //普通用户
    public static final Integer SYS_USER=4;
    //专家
    public static final Integer SYS_EXPERT=5;

    public static final Long JUDGE_FLAG=1L;
    public static final Long MONITOR_FLAG=2L;

    /**
     * 试题类型
     */
    public static final Integer QUESTION_ZHUGUAN=0;
    public static final Integer QUESTION_KEGUAN=1;

    /**
     * 试卷类型
     */
    public static final Integer PAPER_LILUN=0;
    public static final Integer PAPER_QINGXI=1;
    public static final Integer PAPER_SHICAO=2;

    public static final Integer PAPER_BIDA=4;
    public static final Integer PAPER_XUANDA=6;
    public static final Integer PAPER_QIANGDA=5;

    public static final Integer PAPER_ZICE=3;
    public static final Integer PAPER_XUNLIAN=7;

    /**
     * 试卷是否被活动关联
     */
    //关联活动未启动
    public static final Integer PAPER_USERD0=0;
    public static final Integer PAPER_NOTUSERD1=1;
    //关联活动已启动
    public static final Integer PAPER_USERD2=2;
    /**
     *redis 活动消息类型
     *
     */
    public static final String PUBLISH_EXAM="publish_exam";
    public static final String CONCELL_EXAM="concell_exam";
    public static final String JUDGE_EXAM="judge_exam";
    public static final String PUBLISH_SCORE="publish_score";

    /**
     * websocket 消息类型
     */
    public static final String MESSAGE_KAOSHI="KAOSHI:";
    public static final String MESSAGE_JIANKAO="JIANKAO:";
    /**
     * 消息已读状态
     */
    public static final String IS_READ="1";
    public static final String IS_NOT_READ="0";
    //监考
    public static final String IS_MONITOR="back:permission:monitor";
    //阅卷
    public static final String IS_JUDGE="back:permission:judge";

    /**
     * 注解参数类型
     */
    public static final String PARAMETER_STR="str";
    public static final String PARAMETER_MAP="map";

    /**
     * 试题难度系数
     */
    public  static final Double difficulty_1 = 0.2;
    public  static final Double difficulty_2 = 0.4;
    public  static final Double difficulty_3 = 0.6;
    public  static final Double difficulty_4 = 0.8;
    public  static final Double difficulty_5 = 1.0;


    /**
     * 考生抢答试题入口key
     */
    public  static final String race_question_key_prefix = "_racekey";

    /**
     * 课程管理课程状态
     * 0 提交未上课、1上课中、2下课、3结束
     */
    public static final Integer COURSE_INIT = 0;
    public static final Integer COURSE_STARTING = 1;
    public static final Integer COURSE_ENDING = 2;
    public static final Integer COURSE_FINISH = 3;

    /**
     * 学员课后练习状态
     * 0 未提交 1 已提交待批阅 2 已批阅
     */
    public static final Integer COURSE_AFTER_INIT = 0;
    public static final Integer COURSE_AFTER_SUBMIT = 1;
    public static final Integer COURSE_AFTER_FINISH = 2;
    /**
     * 课程练习类型 1 课堂练习 2 课后练习
     */
    public static final Integer COURSE_QUESTION_BEFORE = 0;
    public static final Integer COURSE_QUESTION_AFTER = 1;
    /**
     * weboffice模版路径
     */
    public static final String model_url="/weboffice/webofficeTest/officeFile/test.docx";

    /**
     * 默认添加用户密码
     */
    public static final String password = "123456";

    /**
     * 课程相关角色
     */
    public static String COURSE_TEACHER="teacher";
    public static String COURSE_STUDENT="student";

}
