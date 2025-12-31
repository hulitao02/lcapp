package com.cloud.exam.vo;

import com.cloud.exam.model.exam.ExamPlace;
import com.cloud.exam.model.exam.ManageGroup;
import com.cloud.exam.model.exam.Paper;
import com.cloud.model.user.AppUser;
import com.cloud.model.user.SysDepartment;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dyl on 2021/03/23.
 * 活动单位小组关联表
 */
@Data
public class ExamRelationsVO implements Serializable {

    private Integer acid;
    private Integer departid;
    private Integer managegroupid;
    private Integer paperid;
    private List<SysDepartment> departments;
    private List<Paper> papers;
    private List<ManageGroup>  manageGroups;
    private List<AppUser> users;
    private List<ExamPlace> examPlaces;
}
