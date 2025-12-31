package com.cloud.exam.utils;

import com.cloud.core.ExamConstants;
import com.cloud.exam.model.exam.Exam;
import com.cloud.exam.vo.DrawResultVO;
import com.cloud.utils.DateConvertUtils;

/**
 * Created by dyl on 2021/04/20.
 */
public class UserMessageUtils {

    public static final String  m1 = "您有一个新的考试活动(";
    public static final String  m2 = ",考试开始时间：";
    public static final String  m3 = "结束时间：";
    public static final String  m4 = "分钟,准考证号：";
    public static final String  m5 = ",考试地点：";
    public static final String  m6 = "请提前做好准备。";
    public static final String  m7 = ",座位号：";
    public static final String  m8 = ",考试时长：";
    public static String createMessage(Exam exam, DrawResultVO dr, String flag, Object o){
        String message ="";
        if("1".equals(flag)){
            message = m1+exam.getName()+")"+m2+ DateConvertUtils.date2Str(exam.getStartTime())+m8+dr.getExamTime()+m4+dr.getIdentityCard()+m5+dr.getPlaceName()+m7+dr.getPlaceNum()
                    +","+m6;
        }else if("5".equals(flag)){
            message = "您参加的活动："+exam.getName()+"，此活动已被取消。";
        }else if("6".equals(flag)){
            message ="您参加的活动："+exam.getName()+"已经可以开始考试，请及时登录。";;
        }else if("4".equals(flag)){
            message ="您参加的活动："+exam.getName()+"已经结束,"+o+"试卷您得分"+dr.getScore()+"分。";;
        }
        if(exam.getType().equals(ExamConstants.EXAM_TYPE_XUNLIAN)){
            message = m1+exam.getName()+")"+m2+ DateConvertUtils.date2Str(exam.getStartTime())+","+m6;
        }
        return message;
    }
}
