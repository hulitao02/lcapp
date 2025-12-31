package com.cloud.exception;

/**
 * Created by dyl on 2021/03/26.
 * 异常提示信息
 */
public enum ResultMesCode {

    // 状态码定
    SUCCESS(200, "操作成功。"),
    NO_PERMISSION(403, "缺少权限，请检查。"),
    NO_AUTH(401, "未登录验证用户，请检查。"),
    NOT_FOUND(404, "未找到资源，请检查。"),
    INTERNAL_SERVER_ERROR(500, "请求失败，请重试，或联系管理员"),
    Bad_Request(400, "请求异常，请检查。"),
    CREATED(201, "创建成功。"),
    NOT_ALLOWED(405, "方法不被允许。"),
    UNSUPPORTED_MEDIA_TYPE(415, "请求类型错误。"),
    PARAM_IS_ERROR(20005, "接口参数错误"),
    NULLPOINTER_DATA_WRONG(10000, "您的输入有误，请重新输入..."),
    IDCARD_WRONG(10001, "您的输入有误，请重新输入..."),
    INDENTITY_CARD_WRONG(10002, "您的输入有误，请重新输入..."),
    EXAM_NOT_MEMBER_REL(40000, "活动单位缺少关联人员。"),
    EXAM_NOT_PAPER_REL(40001, "活动单位缺少关联试卷。"),
    EXAM_NOT_PLACE_REL(40002, "活动缺少关联场地。"),
    EXAM_NOT_MANAGER_REL(40003, "活动缺少关联小组。"),
    EXAM_NOT_MANAGER_USER_REL(40004, "活动小组缺少关联人员。"),
    EXAM_LAUNCH_PASS_TIME(40005,"活动时间已过，无法启动。"),
    EXAM_LAUNCH_YES(40006,"活动已经启动。"),
    EXAM_CHILD_NOT_FINISH(40007,"该活动存在还未完成的子活动，暂时无法启动。"),
    EXAM_USER_STATUS_CONCELL(40008,"考试已取消..."),
    EXAM_USER_STATUS_COMMITED(40009,"您当前没有考核..."),
    EXAM_USER_STATUS_FINISHED(40010,"考试已结束..."),
    EXAM_NOT_STARTED(40011,"考试未开始..."),
    ACTIVITY_CHILEREN_NOT_FINISH(400012,"子活动未结束，操作失败"),
    ACTIVITY_NOT_PERMISSION(400013,"您没有权限，操作失败"),
    ACTIVITY_USER_NOT_FINISH(400014,"有成员正在考试中，操作失败"),
    ACTIVITY_PAPER_WAITING_JUDGE(400015,"存在待判卷的记录，操作失败"),
    QUESTION_NUMBER_LESS(400016,"试题数量不够..."),
    EXAM_PLACENUM_LESS(400017,"座位数量不够,请重新设置..."),
    EXAM_EXTTIME_LESS(400018,"子活动时间超出主活动时间,请修改."),
    EXAM_RACE_ACCESSKEYFAIL(400019,"获取抢答权失败."),
    EXAM_RACE_ACCESSKEYSUCCESS(400020,"获取抢答权成功."),
    REQUEST_SERVICE_REPEAT(400021,"请不要重复提交."),
    EXAM_PAPERTIME_OUT(400022,"您的上次登录考试时间距离现在超过试卷考试时间,无法再次进入考试...");
    private Integer resultCode;
    private String resultMsg;

    ResultMesCode(Integer resultCode, String resultMsg) {
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
