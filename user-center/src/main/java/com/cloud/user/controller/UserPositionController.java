package com.cloud.user.controller;


import com.cloud.model.common.Page;
import com.cloud.model.user.UserPosition;
import com.cloud.model.utils.AppUserUtil;
import com.cloud.user.service.UserPositionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Red
 */
@RestController
@RequestMapping("/position")
public class UserPositionController {
    @Resource
    private UserPositionService userPositionService;


    @GetMapping("/findUserPositionList")
    public Page<UserPosition> findUserPositionList(@RequestParam Map<String, Object> params){
        return userPositionService.findUserPosition(params);
    }

    @GetMapping("/findUserPosition")
    public List<UserPosition> findUserPositionList(){
        return userPositionService.findUserPosition();
    }



    @PostMapping("/addPosition")
    public  Integer addPosition(@RequestBody UserPosition userPosition){
        userPosition.setCreator(AppUserUtil.getLoginAppUser().getId());
        userPosition.setCreateTime(new Date());
        userPosition.setUpdateTime(new Date());
        return userPositionService.save(userPosition);
    }

    @PutMapping("/updatePosition")
    public  Integer updatePosition(@RequestBody UserPosition userPosition){
        userPosition.setCreator(AppUserUtil.getLoginAppUser().getId());
        userPosition.setUpdateTime(new Date());
        return userPositionService.update(userPosition);
    }

    @GetMapping("/findUserPositionById/{id}")
    public UserPosition findUserPositionById(@PathVariable Long id){
       return userPositionService.findById(id);
    }


    @DeleteMapping("/deletePositionById/{id}")
    public Integer deletePositionById(@PathVariable Long id){
        return userPositionService.deleteById(id);
    }
}
