package com.cloud.exam.vo;

import com.cloud.exam.model.exam.ManageGroup;
import com.cloud.model.user.AppUser;
import lombok.Data;

import java.util.List;

/**
 * Created by dyl on 2021/04/22.
 */
@Data
public class ManageGroupVO {
    private ManageGroup mg;
    private List<AppUser> appUserVOList;
}
