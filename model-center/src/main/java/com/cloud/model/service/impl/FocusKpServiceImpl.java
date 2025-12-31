package com.cloud.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.model.dao.FocusKpDao;
import com.cloud.model.model.FocusKp;
import com.cloud.model.service.FocusKpService;
import com.cloud.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FocusKpServiceImpl extends ServiceImpl<FocusKpDao, FocusKp>
        implements FocusKpService {

    @Autowired
    private FocusKpDao focusKpDao;

    @Override
    public IPage<FocusKp> selectFocusListForPage(String searchParam, Integer userId, Integer pageNo, Integer pageSize) {
        // 分页参数
        Page<FocusKp> pageParam = new Page();
        pageParam.setCurrent(pageNo);
        pageParam.setSize(pageSize);
//        查询参数
        QueryWrapper<FocusKp> queryWrapper = new QueryWrapper();
        if (StringUtils.isNotEmpty(searchParam)) {
            queryWrapper.like("kp_name", searchParam);
        }
        queryWrapper.eq("status", 1);
        queryWrapper.eq("user_id", userId);
        return this.focusKpDao.selectPage(pageParam, queryWrapper);
    }
}
