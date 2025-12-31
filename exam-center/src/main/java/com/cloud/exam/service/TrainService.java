package com.cloud.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.train.Train;
import com.cloud.exam.vo.RuleBeanVO;


public interface TrainService extends IService<Train> {

    public Boolean saveSelfTrain(RuleBeanVO ruleBeanVO);


}
