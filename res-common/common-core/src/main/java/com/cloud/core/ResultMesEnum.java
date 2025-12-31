package com.cloud.core;

/**
 * Created by dyl on 2021/03/26.
 * 异常提示信息
 */
public enum ResultMesEnum {

    // 状态码定义
    SUCCESS(200, "操作成功。"),
    NO_PERMISSION(403,"缺少权限，请检查"),
    NO_AUTH(401,"未登录验证用户，请检查"),
    NOT_FOUND(404, "未找到资源，请检查"),
    INTERNAL_SERVER_ERROR(500, "服务器异常，请重试"),
    Bad_Request(400,"请求异常，请检查"),
    CREATED(201,"创建成功"),
    NOT_ALLOWED(405,"方法不被允许"),
    UNSUPPORTED_MEDIA_TYPE(415,"请求类型错误"),
    IDCARD_WRONG(10001,"身份证号输入有误，请检查"),
    INDENTITY_CARD_WRONG(10002,"准考证号输入有误，请检查"),
    EXAM_NOT_MEMBER_REL(40000,"活动单位缺少关联人员。。。"),

    EXAM_NOT_PAPER_REL(40001,"活动单位缺少关联试卷。。。"),
    EXAM_NOT_PLACE_REL(40002,"活动单位缺少关联场地。。。"),
    EXAM_NOT_MANAGER_REL(40003,"活动单位缺少关联小组。。。"),
    EXAM_NOT_MANAGER_USER_REL(40004,"活动单位缺少关联小组人员。。。"),
    EXAM_USER_NOT_MATCH(40005,"考生当前具有的权限与已添加的试卷不一致。。。"),
    COURSE_TEACHER_NOT_MATCH(40006,"课程知识点与当前已选择的教员数据权限不一致。。。"),
    COURSE_STUDENT_NOT_MATCH(40007,"课程知识点与当前已选择的考生数据权限不一致。。。"),
    COURSE_KPID_NOT_MATCH(40008,"课程知识点与当前已选择的课堂知识点数据权限不一致。。。"),
    COURSE_QUESTION_NOT_MATCH(40009,"课程知识点与当前已选择试题数据权限不一致。。。"),
    COURSE_KP_NOT_MATCH(40010,"选择的课程知识点与当前已选择试题学员课堂知识点数据权限不一致。。。"),
    COURSE_KP_MATCH(40011,"选择的课程知识点与当前已选择试题学员课堂知识点数据权限一致。。。");
    /** 错误码 */
    private Integer resultCode;

    /** 错误信息 */
    private String resultMsg;

    ResultMesEnum(Integer resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }
}
