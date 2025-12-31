package com.cloud.exam.vo;

import com.cloud.model.user.AppUser;
import com.cloud.model.user.SysDepartment;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyl on 2021/04/23.
 */
@Data
public class SysDepartmentVO {
    private SysDepartment department;

    private List<AppUser> appUserList = new ArrayList<>();

    private List<PaperVO> paperList = new ArrayList<>();

    private List<Long> paperIds = new ArrayList<>();
}
