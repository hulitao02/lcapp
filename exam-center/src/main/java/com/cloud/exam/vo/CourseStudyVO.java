package com.cloud.exam.vo;

import com.cloud.exam.model.course.CourseKpRel;
import com.cloud.exam.model.course.CourseStudy;
import com.cloud.exam.model.exam.Question;
import com.cloud.model.user.AppUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dyl on 2022/01/10.
 */
@Data
public class CourseStudyVO extends CourseStudy {

    public  List<Long> studentIds = new LinkedList<>();
    public  List<CourseKpRel> kpIds = new LinkedList<CourseKpRel>();
    public  List<Long> qIds1 = new LinkedList<>();
    public  List<Question> qIds2 = new LinkedList<>();
    public  String kps ;
    public  List<Long> kpList = new ArrayList<>();

    public List<AppUser> studentDetails;
    public List<CourseKpRel> kpDetails;
    public List<Question> beforeDetails ;
    public List<Question> afterDetails ;
    public String teacherName;
    public Boolean flag ;
}
