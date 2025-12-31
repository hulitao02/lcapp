package com.cloud.exam.strategy;

import com.cloud.core.ExamConstants;
import com.cloud.exam.model.exam.DrawResult;
import com.cloud.exam.model.exam.Exam;
import com.cloud.exam.model.exam.ExamPlace;
import com.cloud.exam.model.exam.Paper;
import com.cloud.exam.service.ExamService;
import com.cloud.exam.service.PaperService;
import com.cloud.exam.utils.SpiltPaperUtils;
import com.cloud.exam.vo.DrawResultVO;
import com.cloud.feign.usercenter.SysDepartmentFeign;
import com.cloud.model.user.AppUser;
import com.cloud.utils.DateConvertUtils;
import com.cloud.utils.RandomCharOrNumUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dyl on 2021/06/16.
 * 竞答活动抽签
 */
@Component
public class CompetitionlExamDraw implements DrawResultStrategy {

    @Autowired
    private SysDepartmentFeign sd;
    private static SysDepartmentFeign sysDepartmentFeign;
    @Autowired
    private  ExamService es;
    private static ExamService examService;
    @Autowired
    private PaperService ps;
    private static PaperService paperService;
    @PostConstruct
    public void init(){
        examService = this.es;
        sysDepartmentFeign = this.sd;
        paperService = this.ps;
    }

    /**
     *
     * @param examId  活动id
     * @param map1  单位-->试卷
     * @param map2  单位-->人员
     * @param placesIds 场地id&场地座位号
     * @param papers  单位试卷
     * @param places  单位场地
     * @return
     */
    @Override
    public List<DrawResultVO> getDrawResult(Long examId, Map<Long, List<Long>> map1, Map<Long, List<Long>> map2, List<String> placesIds, List<Paper> papers, List<ExamPlace> places) {
        Exam exam = examService.getById(examId);
        Integer unit = exam.getUnit();
        Map<Long, String> userPaperDraw= SpiltPaperUtils.spiltplace(unit,map2, placesIds);
        List<DrawResultVO> drawResultVOS = new ArrayList<>();
        List<DrawResult> drawResults = new ArrayList<>();
        for (Long userId:userPaperDraw.keySet()){
            AppUser userVO = sysDepartmentFeign.findAppUserById(userId);
            DrawResultVO drv = new DrawResultVO();
            DrawResult vo = new DrawResult();
            vo.setAcId(examId);
            vo.setPaperId(0L);
            vo.setUserId(userId);
            vo.setIdentityCard(RandomCharOrNumUtils.getCharAndNum(12));
            vo.setUserStatus(ExamConstants.EXAM_NOT_LOGIN);
            vo.setScore(exam.getInitialScore());
            String s = userPaperDraw.get(userId);
            vo.setPlaceId(Integer.valueOf(s.split("&")[0]));
            vo.setPlaceNum(Long.valueOf(s.split("&")[1]) + "");
            vo.setExamType(ExamConstants.EXAM_TYPE_JINGDA);
            drv.setExamName(exam.getName());
            int userCount =  0;
            for (Long departId:map2.keySet()) {
                List<Long> longs = map2.get(departId);
                if (longs.contains(userId)){
                    drv.setDepartName(sysDepartmentFeign.findSysDepartmentById(departId).getDname());
                    vo.setDepartId(departId);
                    userCount = longs.size();
                }
            }
            drv.setUserCount(userCount);
            drv.setUserName(userVO.getNickname());
            //  drv.setPositionName(userVO.getPositionName());
            //  vo.setPaperType(-1);
              places.stream().forEach(p -> {
                if ((Long.valueOf(s.split("&")[0])).equals(p.getId())) {
                    drv.setPlaceName(p.getPlaceName());
                }
            });
            drv.setExamDate(DateConvertUtils.getPattenTime(exam.getStartTime(),exam.getEndTime()));
            BeanUtils.copyProperties(vo,drv);

            if(exam.getUnit()==1){
                //个人考试
                drawResultVOS.add(drv);
            }else if(exam.getUnit()==0){
                //集体考试
                boolean b = drawResultVOS.stream().anyMatch(dr -> dr.getDepartId().equals(userVO.getDepartmentId()));
                if(!b){
                    drawResultVOS.add(drv);
                }
            }
            drawResults.add(vo);


        }
        examService.saveDrawResultVO(drawResults);
        return drawResultVOS;
    }

}
