package com.cloud.user.service;

import com.cloud.model.common.Page;
import com.cloud.model.user.UserPosition;

import java.util.List;
import java.util.Map;

public interface UserPositionService {

    int save(UserPosition userPosition);

    int update(UserPosition userPosition);

    UserPosition findById(Long id);

    int count(Map<String, Object> params);

    Page<UserPosition> findUserPosition(Map<String, Object> params);

    int deleteById(Long id);

    List<UserPosition> findUserPosition();

}
