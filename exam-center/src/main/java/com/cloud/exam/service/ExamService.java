package com.cloud.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.vo.ExamStatisticsVO;
import com.cloud.model.exam.ExamStatisticsDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dyl on 2021/03/22.
 */
public interface ExamService extends IService<Exam> {


    IPage<Exam> findAll(Page pg, QueryWrapper qw);

    List<ExamDepartUserRel> getSysDepartmemtByExamId(Long examId);

    ExamDepartPaperRel findRelations(Long examId, Long departId, Long paperId);

    Integer addExamPaperRelations(Long examId, Long departId, Long paperId);

    Integer addManageGroupRel(ManageGroup mg, Long examId);

    Integer delManageGroup(Long examId, Long mgId);

    Integer delManageGroupMember(Long examId, Long mgId, Long userId);

    Integer delDepart(Long departId, Long examId);

    Integer addExamPlace(Long epid, Long examId);

    Integer delExamPlace(Long epId, Long examId);

    Integer addDepartByexamId(Long examId, Long departId);

    void addDepartUserRel(Long examId, Long departId);

    Integer addMemberByDepartmentId(Long examId, Long departId, Long memberId);

    List<ExamPlace> searchExamPlaceByExamId(Long examId);

    IPage<ManageGroup> searchManageByExamId(Page pg, Long examId);

    List<ExamDepartPaperRel> getDepartAndPaperByExamId(Long examId);

    List<ExamDepartUserRel> getDepartAndUserByExamId(Long examId);

    List<ExamPlaceRel> getExamPlaceRelByExamId(Long examId);

    void saveDrawResultVO(List<DrawResult> drv);

    List<Paper> getAllPaper(Long examId);

    List<ExamPlace> getAllExamPlace(Long examId);

    void updateDrawResult(DrawResult dr);

    List<DrawResult> getMinitorResult(Long examId);

    List<Question> getAllQuestionsByIdentityId(String identityId);

    List<ExamDepartUserRel> getExamDepartRelations(Long departmentId, Long userId);

    List<ExamManageGroupRel> getAllExamManageRel(Long userId);

    Integer updateManageGroupRel(Long mgId, Long examId, Long userId);

    List<ExamManageGroupRel> getAllExamManageRelByExamId(Long examId);

    void delManageGroupRel(Long examId, Long mgId);

    List<Exam> getExamByUserid(Long userId);

    List<Exam> getExamPlaceRelations(String name);

    Exam getExamDescribeByIdentityCard(String identityCard);

    Integer delDepartUser(Long departId, Long examId, Long userId);

    Integer updateDepartUser(Long departId, Long examId, Long userId);

    List<ExamManageGroupRel> getManageRel(Long examId, Long mgId);

    void updateManageGroupMember(Long examId, Long mgId, Long userId);


    void deleteDepartPaperRel(Long examId, Long departId);

    List<Paper> listPapers();


    List<ExamDepartUserRel> getExamDepartRel(Long examId, Long departId);


    ExamDepartPaperRel findByPaperId(long paperId);


    Integer delDepartRel(Long departId, Long examId);

    List<Question> getAllQuestionsBypaperId(Long paperId);

    List<ExamManageGroupRel> getMinitorUserByExamId(Long examId);

    void deleteExtDepartPaperRel(Long examId, Long departId);

    Set<Long> getAllExamRelByCurrentUser(Long userId);

    void addCompetitionPaperRel(Long examId, Long paperId, Integer paperType);

    List<Exam> getEexmByPaperId(Long id);

    List<ExamStatisticsVO> getExamListByTypeAndStatus(ExamStatisticsDto examStatisticsDto);




    /**
     *  正在参加考试的人
     * @param examDTO
     * @return
     */
    List<Map> getExamingUserList(ExamStatisticsDto examDTO);
    /**
     *  统计考试创建
     * @param examDTO
     * @return
     */
    List<Map<String,Object>> getStatisticsPaperCountCreated(ExamStatisticsDto examDTO);
    List<Map<String,Object>> getStatisticsPaperCountUsed(ExamStatisticsDto examDTO);

    ///////////最新统计方法，上边的需要替换带/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    List<Map<String,Object>> getStatisticsExamCountByTypeAndStatus(ExamStatisticsDto examDTO);
    List<Map<String,Object>> getStatisticsExPaperCountByTypeAndStatus(ExamStatisticsDto examDTO);
    List<Map<String,Object>> getStatisticsExUserCountByTypeAndStatus(ExamStatisticsDto examDTO);

//    已考试的维度 统计
    List<Map<String,Object>> getStatisticsExUserCountGroupExId(ExamStatisticsDto examDTO);
//    已考试的维度 试卷统计统计
    List<Map<String, Object>> getStatisticsExPaperCountGroupExId(ExamStatisticsDto examDTO);

    //    已考试的维度，试题统计
    List<Map<String,Object>> getStatisticsExPQCountGroupExId(ExamStatisticsDto examDTO);
    //  自测 特殊类型 。
    List<Map<String,Object>> getStatisticsSelfExUserCountGroupExId(ExamStatisticsDto examDTO);

//     用户分组，统计用户的考试总量
    List<Map<String, Object>> getStatisticsExCountGroupUserId(ExamStatisticsDto examDTO);
//     用户分组，统计用户的试题总量
    List<Map<String, Object>> getStatisticsQesCountGroupUserId(ExamStatisticsDto examDTO);
    /**
     *  自测
     * @param examDTO
     * @return
     */
    List<Map<String, Object>> getStatisticsSelfExCountGroupUserId(ExamStatisticsDto examDTO);
    List<Map<String, Object>> getStatisticsSelfQesCountGroupUserId(ExamStatisticsDto examDTO);


    List<ExamStatisticsVO> getExamAndUserByTypeAndStatus(ExamStatisticsDto examDTO);

    public List<Map<String, Object>> getStaticExUserBystatusAndType(ExamStatisticsDto examDTO);


    List<DrawResult> getUserTrainTimeAndRank(String date);

    List<ExamDepartPaperRel> findPaperByexamIdanddepartId(Long examId, Long departId);

    List<ExamDepartUserRel> findUserByByexamIdanddepartId(Long examId, Long departId);

    Map<Long, String> isUserRelated(List<Long> userIdList);
}
