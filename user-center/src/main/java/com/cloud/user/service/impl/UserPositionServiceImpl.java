package com.cloud.user.service.impl;

import com.cloud.model.common.Page;
import com.cloud.model.user.UserPosition;
import com.cloud.user.dao.AppUserDao;
import com.cloud.user.dao.UserPositionDao;
import com.cloud.user.service.UserPositionService;
import com.cloud.utils.PageUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Red
 */
@Service
public class UserPositionServiceImpl implements UserPositionService {

    @Resource
    private UserPositionDao userPositionDao;

    @Resource
    private AppUserDao appUserDao;

    @Override
    public int save(UserPosition userPosition) {
        return userPositionDao.save(userPosition);
    }

    @Override
    public int update(UserPosition userPosition) {
        return userPositionDao.update(userPosition);
    }

    @Override
    public UserPosition findById(Long id) {
        return userPositionDao.findById(id);
    }

    @Override
    public int count(Map<String, Object> params) {
        return userPositionDao.count(params);
    }

    @Override
    public Page<UserPosition> findUserPosition(Map<String, Object> params) {
        Integer count = userPositionDao.count(params);
        List<UserPosition> list = Collections.emptyList();
        if (count > 0) {
            PageUtil.pageParamConver(params, true);

            list = userPositionDao.findData(params);
        }
        return new Page<>(count, list);
    }

    @Override
    public int deleteById(Long id) {
        //删除前判断是否存在用户绑定
        List<Long> positionIdList = appUserDao.positionList();
        if (positionIdList.contains(id)){
            throw new IllegalArgumentException("该职位下存在用户，无法删除！");
        }
        return userPositionDao.deleteById(id);
    }

    @Override
    public List<UserPosition> findUserPosition() {
        return userPositionDao.findUserPosition();
    }
}
