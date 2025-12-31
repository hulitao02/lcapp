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
import java.util.Random;

/**
 * Created by dyl on 2021/06/16.
 * 常规活动抽签
 */
@Component
public class NomalExamDraw implements DrawResultStrategy {

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
    @Override
    public List<DrawResultVO> getDrawResult(Long examId, Map<Long, List<Long>> map1, Map<Long, List<Long>> map2, List<String> placesIds, List<Paper> papers, List<ExamPlace> places) {
        Exam exam  = examService.getById(examId);
        int totalPlaceNum = placesIds.size();
        List<DrawResultVO> drawResultVOS = new ArrayList<>();
        List<DrawResult> drawResults = new ArrayList<>();
        for (Long departId : map1.keySet()) {
            List<Long> paperIds = map1.get(departId);
            List<Long> userIds = map2.get(departId);
            Map<Long, Long> userPaperDraw = SpiltPaperUtils.SpiltPaper(userIds, paperIds);
            for (Long id : userPaperDraw.keySet()) {
                AppUser userVO = sysDepartmentFeign.findAppUserById(id);
                DrawResultVO drv = new DrawResultVO();
                DrawResult vo = new DrawResult();
                Random r = new Random();
                String s = placesIds.get(r.nextInt(totalPlaceNum));
                vo.setAcId(examId);
                vo.setUserId(id);
                drv.setExamName(exam.getName());
                drv.setUserName(userVO.getNickname());
                drv.setPositionName(userVO.getPositionName());
                vo.setIdentityCard(RandomCharOrNumUtils.getCharAndNum(12));
                vo.setDepartId(departId);
                vo.setUserStatus(ExamConstants.EXAM_NOT_LOGIN);
                drv.setDepartName(sysDepartmentFeign.findSysDepartmentById(departId).getDname());
                vo.setPaperId(userPaperDraw.get(id));
                vo.setPaperType(paperService.getById(userPaperDraw.get(id)).getType());
                vo.setExamType(ExamConstants.EXAM_TYPE_LILUN);
                papers.stream().forEach(p -> {
                    if (userPaperDraw.get(id).equals(p.getId())) {
                        drv.setPaperName(p.getPaperName());
                    }
                });
                vo.setPlaceId(Integer.valueOf(s.split("&")[0]));
                places.stream().forEach(p -> {
                    if ((Long.valueOf(s.split("&")[0])).equals(p.getId())) {
                        drv.setPlaceName(p.getPlaceName());
                    }
                });
                vo.setPlaceNum(Long.valueOf(s.split("&")[1]) + "");
                drv.setExamDate(DateConvertUtils.getPattenTime(exam.getStartTime(),exam.getEndTime()));
                BeanUtils.copyProperties(vo,drv);
                drawResultVOS.add(drv);
                drawResults.add(vo);
                placesIds.remove(s);
                totalPlaceNum--;
            }
        }
        examService.saveDrawResultVO(drawResults);
        return drawResultVOS;
    }
}
