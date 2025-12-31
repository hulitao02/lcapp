package com.cloud.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.exam.dao.*;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.service.ExamService;
import com.cloud.exam.vo.ExamStatisticsVO;
import com.cloud.model.exam.ExamStatisticsDto;
import com.cloud.model.exam.UserRelatedDto;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dyl on 2021/03/22.
 */
@Service
@Transactional
public class ExamServiceImpl extends ServiceImpl<ExamDao, Exam> implements ExamService {

    @Autowired
    private ExamDao examDao;
    @Autowired
    private ManageGroupDao manageGroupDao;
    @Autowired
    private ExamPlaceDao examPlaceDao;
    @Autowired
    private DrawResultDao drawResultDao;
    @Autowired
    private UserActivityMessageDao userActivityMessageDao;
    @Override
    public IPage<Exam> findAll(Page pg, QueryWrapper exame) {
        return examDao.findAll(pg, exame);
    }

    @Override
    public  List<ExamDepartUserRel> getSysDepartmemtByExamId(Long examId){
        return examDao.getSysDepartmemtByExamId(examId);
    }
    @Override
    public ExamDepartPaperRel findRelations(Long examId, Long departId, Long paperId) {
        return examDao.findRelations(examId, departId, paperId);
    }

    @Override
    public Integer addExamPaperRelations(Long examId, Long departId, Long paperId) {
        return examDao.addExamPaperRelations(examId, departId, paperId);
    }

    @Override
    public Integer delDepart(Long departId, Long examId) {
        return examDao.delDepart(departId, examId);
    }

    @Override
    public Integer addManageGroupRel(ManageGroup mg, Long examId) {
        Integer res1 = examDao.addRelations(mg.getId(), examId);
        return res1;
    }

    @Override
    public List<ExamManageGroupRel> getAllExamManageRelByExamId(Long examId){
        return examDao.getAllExamManageRelByExamId(examId);
    }

    @Override
    public Integer delManageGroup(Long examId, Long id) {
        return examDao.delManageGroup(examId, id);
    }

    @Override
    public Integer delManageGroupMember(Long examId, Long mgId, Long userId) {
        return examDao.delManageGroupMember(examId, mgId, userId);
    }

    @Override
    public void delManageGroupRel(Long examId,Long mgId){
        examDao.delManageGroupRel(examId, mgId);
    }
    @Override
    public Integer addExamPlace(Long epId, Long examId) {
        Integer integer =  examDao.addExamPlaceRelations(epId, examId);
        return integer;
    }

    @Override
    public Integer delExamPlace(Long epId, Long examId) {
        return examDao.delExamPlace(epId, examId);
    }

    @Override
    public Integer addDepartByexamId(Long examId, Long departId) {
        return examDao.addDepartByexamId(examId, departId);
    }

    @Override
    public Integer addMemberByDepartmentId(Long examId, Long departId, Long memberId) {
        return examDao.addMemberByDepartmentId(examId, departId,memberId);
    }

    @Override
    public List<ExamPlace> searchExamPlaceByExamId(Long examId) {

        return examDao.searchExamPlaceByExamId(examId);
    }

    @Override
    public IPage<ManageGroup> searchManageByExamId(Page pg, Long examId) {

        return examDao.searchManageByExamId(pg, examId);
    }

    @Override
    public List<ExamDepartPaperRel> getDepartAndPaperByExamId(Long examId) {

        return examDao.getDepartAndPaperByExamId(examId);
    }

    @Override
    public List<ExamDepartUserRel> getDepartAndUserByExamId(Long examId) {
        return examDao.getDepartAndUserByExamId(examId);
    }

    @Override
    public List<ExamPlaceRel> getExamPlaceRelByExamId(Long examId) {
        return examDao.getExamPlaceRelByExamId(examId);
    }

    @Override
    public void saveDrawResultVO(List<DrawResult> vo) {
        for (DrawResult v : vo) {
            drawResultDao.insert(v);
        }
    }

    @Override
    public List<Paper> getAllPaper(Long examId) {
        return examDao.getAllPaper(examId);
    }

    @Override
    public List<ExamPlace> getAllExamPlace(Long examId) {
        return examDao.getAllExamPlace(examId);
    }

    @Override
    public void updateDrawResult(DrawResult dr) {
        examDao.updateDrawResult(dr);
    }

    @Override
    public List<DrawResult> getMinitorResult(Long examId) {
        return examDao.getMinitorResult(examId);
    }


    @Override
    public List<Question> getAllQuestionsByIdentityId(String identityId) {
        return examDao.getAllQuestionsByIdentityId(identityId);
    }
    @Override
    public List<Question> getAllQuestionsBypaperId(Long paperId){
        return examDao.getAllQuestionsBypaperId(paperId);
    }
    @Override
    public List<ExamDepartUserRel> getExamDepartRelations(Long departmentId, Long userId) {

        return examDao.getExamDepartRelations(departmentId, userId);
    }

    @Override
    public List<ExamManageGroupRel> getAllExamManageRel(Long userId){
        return examDao.getAllExamManageRel(userId);
    }

    @Override
    public Integer updateManageGroupRel(Long mgId,Long examId,Long userId){
        return examDao.updateManageGroupRel(mgId,examId,userId);
    }
    @Override
    public List<Exam> getExamByUserid(Long userId) {
        return examDao.getExamByUserid(userId);
    }

    @Override
    public List<Exam> getExamPlaceRelations(String name){
        return examDao.getExamPlaceRelations(name);
    }

    @Override
    public Exam getExamDescribeByIdentityCard(String identityCard){
        return examDao.getExamDescribeByIdentityCard(identityCard);
    }

    @Override
    public List<ExamManageGroupRel> getManageRel(Long examId,Long mgId){
        return examDao.getManageRel(examId,mgId);
    }
    @Override
    public void updateManageGroupMember(Long examId,Long mgId, Long userId){
        examDao.updateManageGroupMember(examId,mgId,userId);
    }

    @Override
    public  Integer delDepartUser(Long departId,Long examId,Long userId){
        return examDao.delDepartUser(departId,examId,userId);
    }
    @Override
    public  Integer updateDepartUser(Long departId,Long examId,Long userId){
        return examDao.updateDepartUser(departId,examId,userId);
    }
    @Override
    public  void deleteDepartPaperRel(Long examId,Long departId){
        examDao.deleteDepartPaperRel(examId,departId);
    }

    @Override
    public List<Paper> listPapers() {
        return examDao.listPapers();
    }

    @Override
    public List<ExamDepartUserRel>  getExamDepartRel(Long examId, Long departId){
        return examDao.getExamDepartRel(examId,departId);
    }

    @Override
    public void addDepartUserRel(Long examId, Long departId){
        examDao.addDepartUserRel(examId,departId);
    }

    @Override
    public ExamDepartPaperRel findByPaperId(long paperId) {
        return examDao.findByPaperId(paperId);
    }

    @Override
    public Integer delDepartRel(Long departId,Long examId){
        return examDao.delDepartRel(departId,examId);
    }
    @Override
    public List<ExamManageGroupRel> getMinitorUserByExamId(Long examId){
        return examDao.getMinitorUserByExamId(examId);
    }
    @Override
    public void deleteExtDepartPaperRel(Long examId, Long departId){
        examDao.deleteExtDepartPaperRel(examId,departId);
    }
    @Override
    public Set<Long> getAllExamRelByCurrentUser(Long userId){
        return examDao.getAllExamRelByCurrentUser(userId);
    }

    @Override
    public void addCompetitionPaperRel(Long examId, Long paperId, Integer paperType) {
        examDao.addCompetitionPaperRel(examId, paperId,paperType);
    }

    @Override
    public List<Exam> getEexmByPaperId(Long id) {
        return examDao.getEexmByPaperId(id);
    }

    @Override
    public List<ExamStatisticsVO> getExamListByTypeAndStatus(ExamStatisticsDto dto) {
        return this.examDao.getExamListByTypeAndStatus(dto);
    }

    /**
     *  是否有 自测
     * @param examStatisticsDto
     * @return
     */
    @Override
    public List<Map> getExamingUserList(ExamStatisticsDto examStatisticsDto) {
        List<Integer> examTypeList = examStatisticsDto.getExamTypeList();
        if(CollectionUtils.isNotEmpty(examTypeList) && examTypeList.contains(3)){
            return this.examDao.getSelfExamingUserList(examStatisticsDto);
        }
        return this.examDao.getExamingUserList(examStatisticsDto);
    }

    /**
     *  考核类型不同，使用的试卷关联表也不相同
     *   TODO 调整3/24
     * @param examStatisticsDto
     * @return
     */
    @Override
    public List<Map<String, Object>> getStatisticsPaperCountCreated(ExamStatisticsDto examStatisticsDto) {

        if(Objects.nonNull(examStatisticsDto)){
            List<Integer> examTypeList = examStatisticsDto.getExamTypeList();
            if(CollectionUtils.isNotEmpty(examTypeList) && examTypeList.contains(1)){
                // 竞答 1 ， 试卷的关联表是不同的
                return this.examDao.getCompetiPCountCreated(examStatisticsDto);
            }
        }
//        非JD 活动
        return this.examDao.getNotCompetiPCountCreated(examStatisticsDto);
    }

    /**
     *  新增类型判断 ，JD活动和其他 试卷使用情况
     * @param examStatisticsDto
     * @return
     */
    @Override
    public List<Map<String, Object>> getStatisticsPaperCountUsed(ExamStatisticsDto examStatisticsDto) {
        if(Objects.nonNull(examStatisticsDto)){
            List<Integer> examTypeList = examStatisticsDto.getExamTypeList();
            if(CollectionUtils.isNotEmpty(examTypeList) && examTypeList.contains(1)){
                return this.examDao.getCompetiPCountUsed(examStatisticsDto);
            }
        }
        return this.examDao.getStatisticsPaperCountUsed(examStatisticsDto);
    }

    @Override
    public List<Map<String, Object>> getStatisticsExamCountByTypeAndStatus(ExamStatisticsDto examDTO) {
        return this.examDao.getStatisticsExamCountByTypeAndStatus(examDTO);
    }

    @Override
    public List<Map<String, Object>> getStatisticsExPaperCountByTypeAndStatus(ExamStatisticsDto examDTO) {
        return this.examDao.getStatisticsExPaperCountByTypeAndStatus(examDTO);
    }

    @Override
    public List<Map<String, Object>> getStatisticsExUserCountByTypeAndStatus(ExamStatisticsDto examDTO) {
        return this.examDao.getStatisticsExUserCountByTypeAndStatus(examDTO);
    }

    @Override
    public List<Map<String, Object>> getStatisticsExUserCountGroupExId(ExamStatisticsDto examDTO) {
        return this.examDao.getStatisticsExUserCountGroupExId(examDTO);
    }

    @Override
    public List<Map<String, Object>> getStatisticsExPaperCountGroupExId(ExamStatisticsDto examDTO) {
        return this.examDao.getStatisticsExPaperCountGroupExId(examDTO);
    }

    /**
     *  活动关联的 试卷区别竞答和非竞答的
     * @param examDTO
     * @return
     */
    @Override
    public List<Map<String, Object>> getStatisticsExPQCountGroupExId(ExamStatisticsDto examDTO) {
        if(Objects.nonNull(examDTO)){
            List<Integer> examTypeList = examDTO.getExamTypeList();
            if(CollectionUtils.isNotEmpty(examTypeList) && examTypeList.contains(1)){
                return this.examDao.getStatisticsExPQCountGroupExId(examDTO);
            }
        }
        return this.examDao.getNotCompetiExPQCountGroupExId(examDTO);
    }

    @Override
    public List<Map<String, Object>> getStatisticsSelfExUserCountGroupExId(ExamStatisticsDto examDTO) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getStatisticsExCountGroupUserId(ExamStatisticsDto examDTO) {
        return this.examDao.getStatisticsExCountGroupUserId(examDTO);
    }

    @Override
    public List<Map<String, Object>> getStatisticsQesCountGroupUserId(ExamStatisticsDto examDTO) {
        return this.examDao.getStatisticsQesCountGroupUserId(examDTO);
    }

    @Override
    public List<Map<String, Object>> getStatisticsSelfExCountGroupUserId(ExamStatisticsDto examDTO) {
        return this.examDao.getStatisticsSelfExCountGroupUserId(examDTO);
    }

    @Override
    public List<Map<String, Object>> getStatisticsSelfQesCountGroupUserId(ExamStatisticsDto examDTO) {
        return this.examDao.getStatisticsSelfQesCountGroupUserId(examDTO);
    }

    /**
     *  查询活动 和 相关人员的信息
     * @param examDTO
     * @return
     */
    @Override
    public List<ExamStatisticsVO> getExamAndUserByTypeAndStatus(ExamStatisticsDto examDTO) {
        return this.examDao.getExamAndUserByTypeAndStatus(examDTO);
    }


    @Override
    public List<Map<String,Object>> getStaticExUserBystatusAndType(ExamStatisticsDto examDTO) {
        return this.examDao.getStaticExUserBystatusAndType(examDTO);
    }

    @Override
    public List<DrawResult> getUserTrainTimeAndRank(String date) {
        return this.examDao.getUserTrainTimeAndRank(date);
    }

    @Override
    public List<ExamDepartPaperRel> findPaperByexamIdanddepartId(Long examId, Long departId) {
        return this.examDao.findPaperByexamIdanddepartId(examId, departId);
    }

    @Override
    public List<ExamDepartUserRel> findUserByByexamIdanddepartId(Long examId, Long departId) {
        return this.examDao.findUserByByexamIdanddepartId(examId, departId);
    }

    @Override
    public Map<Long, String> isUserRelated(List<Long> userIdList) {
        if (CollectionUtils.isNotEmpty(userIdList)) {
            String inStr = userIdList.stream().map(String::valueOf)
                    .collect(Collectors.joining(",", "(", ")"));
            List<UserRelatedDto> userRelatedDtoList = examDao.isUserRelated(inStr);
            if (CollectionUtils.isNotEmpty(userRelatedDtoList)) {
                Map<Long, String> relatedMap = userRelatedDtoList.stream()
                        .collect(Collectors.toMap(UserRelatedDto::getUserid, UserRelatedDto::getMsg, (o, u) -> o + "," + u));
                return relatedMap;
            }
        }
        return Collections.emptyMap();
    }


}
