package com.cloud.user;

import com.cloud.model.user.AppUser;
import com.cloud.user.dao.AppUserDao;
import com.cloud.user.service.AppUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;


@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class UserTest {


    @Autowired
    private AppUserDao userDao;
    @Autowired
    private AppUserService appUserService;


    @Test
    public void saveTest() {
//      批量保存
        AppUser save1 = new AppUser();
        save1.setUsername("test_boolean");
        save1.setNickname("测试boolean类型");
//        save1.setEnabled(1);
        save1.setStatus(1);
        save1.setCreateTime(new Date());
        save1.setUpdateTime(new Date());
        save1.setType("Type");
        this.appUserService.saveOrUpdate(save1);
    }




//    @Test
//    public void findAppUser() {
//        AppUser appUser = this.userDao.findById(Long.valueOf(1));
//        System.out.println("SQL查询直接执行:  " + JSON.toJSONString(appUser));
//        QueryWrapper wrapper = new QueryWrapper<AppUser>();
//        wrapper.eq("username", "李梦考试3");
//        List appUserList = this.userDao.selectList(wrapper);
//        System.out.println("查询API测试: " + JSON.toJSONString(appUserList));
//    }
//
//
//
//
//    @Test
//    public void updateTest() {
//
//        AppUser appUser = new AppUser();
//        appUser.setId(Long.valueOf(1));
//        appUser.setUsername("李亚东_updated");
//        int update = this.userDao.updateById(appUser);
//        log.info("更新后结果 : {} ", update);
//
//        AppUser update1 = new AppUser();
//        update1.setUsername("saveOrUptate ");
//        update1.setCreateTime(new DateTime());
//        boolean b = this.appUserService.saveOrUpdate(update1);
//        log.info("saveOrUpate: {} ", JSON.toJSONString(update1));
//    }


//    @Test
//    public void deleteTest() {
//        int i = this.userDao.deleteById(Long.valueOf(1));
//        log.info("API 删除: {} ", i);
//    }





}
