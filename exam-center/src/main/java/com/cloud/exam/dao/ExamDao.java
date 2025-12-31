package com.cloud.exam.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.exam.model.exam.*;
import com.cloud.exam.vo.ExamStatisticsVO;
import com.cloud.model.exam.ExamStatisticsDto;
import com.cloud.model.exam.UserRelatedDto;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dyl on 2021/03/22.
 */
@Mapper
public interface ExamDao extends BaseMapper<Exam> {

    IPage<Exam> findAll(Page page, @Param(Constants.WRAPPER) QueryWrapper exame);

    @Select("select * from exam_depart_paper_rel r where r.ac_id=#{examId} and r.depart_id =#{departId} and r.paper_id =#{paperId}")
    ExamDepartPaperRel findRelations(@Param("examId") Long examId, @Param("departId") Long departId, @Param("paperId") Long paperId);

    @Select("select * from exam_depart_user_rel where ac_id=#{examId}")
    List<ExamDepartUserRel> getSysDepartmemtByExamId(Long examId);

    @Select("select * from exam_depart_user_rel where ac_id=#{examId} and depart_id=#{departId}")
    List<ExamDepartUserRel> getExamDepartRel(@Param("examId") Long examId, @Param("departId") Long departId);

    @Insert("insert into exam_depart_user_rel(ac_id,depart_id) values(#{examId},#{departId})")
    void addDepartUserRel(@Param("examId") Long examId, @Param("departId") Long departId);

    @Insert("insert into exam_depart_paper_rel(ac_id,depart_id) values(#{ac_id},#{depart_id})")
    Integer addDepartByexamId(@Param("ac_id") Long ac_id, @Param("depart_id") Long depart_id);

    @Select("select * from exam_depart_user_rel where depart_id=#{departId} and member_id !=#{userId}")
    List<ExamDepartUserRel> getExamDepartRelations(@Param("departId") Long departId, @Param("userId") Long userId);

    @Insert("insert into exam_depart_paper_rel values (#{examId},#{departId},#{paperId})")
    Integer addExamPaperRelations(@Param("examId") Long examId, @Param("departId") Long departId, @Param("paperId") Long paperId);

    @Delete("delete from exam_depart_user_rel where ac_id=#{examId} and depart_id=#{departId} and member_id is null")
    Integer delDepart(@Param("departId") Long departId, @Param("examId") Long examId);

    @Insert("insert into exam_manage_group_rel(ac_id,managegroup_id) values (#{ac_id},#{managegroupid})")
    Integer addRelations(@Param("managegroupid") Integer managegroupid, @Param("ac_id") Long ac_id);

    @Select("select * from exam_manage_group_rel where ac_id=#{examId}")
    List<ExamManageGroupRel> getAllExamManageRelByExamId(Long examId);

    @Delete("delete from manage_group where id=#{id}" +
            ";delete from exam_manage_group_rel where ac_id=#{examId} and managegroup_id=#{id}")
    Integer delManageGroup(@Param("examId") Long examId, @Param("id") Long id);

    @Select("select * from exam_manage_group_rel where ac_id=#{examId} and managegroup_id=#{mgId}")
    List<ExamManageGroupRel> getManageRel(@Param("examId") Long examId, @Param("mgId") Long mgId);

    @Delete("delete from exam_manage_group_rel  where ac_id =#{examId} and managegroup_id =#{mgId} and member_id=#{userId}")
    Integer delManageGroupMember(@Param("examId") Long examId, @Param("mgId") Long mgId, @Param("userId") Long userId);

    @Update("update exam_manage_group_rel set member_id = null where ac_id =#{examId} and managegroup_id =#{mgId} and member_id=#{userId}")
    void updateManageGroupMember(@Param("examId") Long examId, @Param("mgId") Long mgId, @Param("userId") Long userId);

    @Insert("insert into exam_place_rel values(#{ac_id},#{examplace_id})")
    Integer addExamPlaceRelations(@Param("examplace_id") Long examplace_id, @Param("ac_id") Long ac_id);

    @Delete("delete from exam_place where id=#{epId}" +
            ";delete from exam_place_rel where ac_id=#{examId} and examplace_id=#{epId}")
    Integer delExamPlace(@Param("epId") Long epId, @Param("examId") Long id);

    @Update("insert into exam_depart_user_rel values(#{ac_id},#{depart_id},#{memberId})")
    Integer addMemberByDepartmentId(@Param("ac_id") Long ac_id, @Param("depart_id") Long depart_id, @Param("memberId") Long memberId);

    @Select("select ec.* from exam_place ec inner join exam_place_rel rl on ec.id= rl.examplace_id where rl.ac_id = #{ac_id}")
    List<ExamPlace> searchExamPlaceByExamId(@Param("ac_id") Long ac_id);

    @Select("select mg.*,rl.member as id from manage_group mg inner join exam_manage_group_rel rl on mg.id= rl.managegroupid where rl.ac_id = #{ac_id}")
    IPage<ManageGroup> searchManageByExamId(Page page, @Param("ac_id") Long ac_id);

    @Delete("delete from exam_manage_group_rel where ac_id=#{examId} and managegroup_id=#{mgId} and member_id is null")
    void delManageGroupRel(@Param("examId") Long examId, @Param("mgId") Long mgId);

    @Insert("insert into  exam_manage_group_rel values(#{examId},#{mgId},#{userId})")
    Integer updateManageGroupRel(@Param("mgId") Long mgId, @Param("examId") Long examId, @Param("userId") Long userId);

    @Select("select * from exam_depart_paper_rel pr where pr.ac_id=#{ac_id}")
    List<ExamDepartPaperRel> getDepartAndPaperByExamId(Long ac_id);

    @Select("select ur.ac_id as acId,ur.depart_id as departId ,ur.member_id as memberId from exam_depart_user_rel ur where ur.ac_id=#{ac_id}")
    List<ExamDepartUserRel> getDepartAndUserByExamId(Long ac_id);

    @Select(" select pr.ac_id,pr.examplace_id as placeId,ep.place_name as placeName,ep.seat_count as seatCount" +
            " from  exam_place_rel pr inner join exam_place ep on pr.examplace_id = ep.id " +
            " where pr.ac_id=#{ac_id} and pr.examplace_id is not null ")
    List<ExamPlaceRel> getExamPlaceRelByExamId(Long ac_id);

    @Select("select p.paper_name ,p.id from paper p inner join exam_depart_paper_rel pr on p.id=pr.paper_id where pr.ac_id=#{examId}")
    List<Paper> getAllPaper(Long examId);

    @Select("select ep.id ,ep.place_name from exam_place ep inner join exam_place_rel pr on ep.id=pr.examplace_id where pr.ac_id=#{examId}")
    List<ExamPlace> getAllExamPlace(Long examId);

    @Update("update drawresult set exam_status = #{examstatus} where ac_id=#{id}")
    void updateDrawResult(DrawResult d);

    @Select("select r.user_name,r.depart_name,r.place_name,r.paper_type,r.exam_status from drawresult r where r.ac_id=#{examId}")
    List<DrawResult> getMinitorResult(Long examId);

    @Select(" select q_d.id,q_d.question,q_d.options,q_d.type from question q_d  " +
            " where q_d.id in (select DISTINCT q.id from question q inner join paper_manage_rel pmr on q.id=pmr.question_id" +
            "  inner join drawresult dr on dr.paper_id =pmr.paper_id  where dr.identity_card=#{identityId})")
    List<Question> getAllQuestionsByIdentityId(String identityId);

    @Select("select q_1.id,q_1.question, q_1.options, q_1.type,q_1.model_url from question q_1 " +
            " where q_1.id in (select DISTINCT q.id from question q inner join paper_manage_rel pmr on q.id=pmr.question_id where pmr.paper_id=#{paperId})")
    List<Question> getAllQuestionsBypaperId(Long paperId);

    @Select("select * from exam_manage_group_rel where member_id = #{userId}")
    List<ExamManageGroupRel> getAllExamManageRel(Long userId);

    @Select("select e.start_time,e.end_time from exam e inner join exam_depart_user_rel edp on e.id = edp.ac_id " +
            " where edp.member_id=#{userId} and e.exam_status not in (4,5)")
    List<Exam> getExamByUserid(Long userId);

    @Select("select e.start_time,e.end_time from exam e inner join exam_place_rel pl on e.id = pl.ac_id " +
            " inner join exam_place ep on pl.examplace_id = ep.id where  ep.place_name  =#{name} and e.exam_status not in (4,5)")
    List<Exam> getExamPlaceRelations(String name);

    @Select("select e.start_time,e.end_time,dr.paper_name from exam e inner join drawresult dr on e.id=dr.ac_id where dr.identity_card=#{identityCard}")
    Exam getExamDescribeByIdentityCard(String identityCard);

    @Delete("delete from exam_manage_group_rel where ac_id=#{examId}")
    void deleteManageGroupRel(Long examId);

    @Delete("delete from  exam_depart_user_rel  where ac_id=#{examId} and depart_id=#{departId} and member_id=#{userId}")
    Integer delDepartUser(@Param("departId") Long departId, @Param("examId") Long examId, @Param("userId") Long userId);

    @Update("update exam_depart_user_rel set member_id = null where ac_id=#{examId} and depart_id=#{departId} and member_id=#{userId}")
    Integer updateDepartUser(@Param("departId") Long departId, @Param("examId") Long examId, @Param("userId") Long userId);

    @Delete("delete from exam_depart_paper_rel where ac_id=#{examId} and depart_id=#{departId}")
    void deleteDepartPaperRel(@Param("examId") Long examId, @Param("departId") Long departId);

    @Select("select id,paper_name from paper")
    List<Paper> listPapers();

    @Select("select * from DrawResult t where t.admissionnum = #{admissionNum}")
    DrawResult findByAdmissionNum(String admissionNum);

    @Select("select * from exam_depart_paper_rel t where t.paper_id = #{paperId}")
    ExamDepartPaperRel findByPaperId(long paperId);

    @Update("update exam e set e.examstatus = #{examStatus} where e.id=#{id}")
    Integer updateExamStatus(Exam exam);

    @Delete("delete from exam_depart_paper_rel where ac_id=#{examId} and depart_id=#{departId};" +
            "delete from exam_depart_user_rel where ac_id=#{examId} and depart_id=#{departId}")
    Integer delDepartRel(@Param("departId") Long departId, @Param("examId") Long examId);


    @Select("select * from exam where id = #{acId}")
    Exam findExamById(Long acId);

    @Delete("delete from exam_depart_paper_rel where ac_id=#{examId} and depart_id=#{departId} and paper_id is null")
    void deleteExtDepartPaperRel(@Param("examId") Long examId, @Param("departId") Long departId);

    @Select("select * from exam_manage_group_rel where ac_id  = #{examId}")
    List<ExamManageGroupRel> getMinitorUserByExamId(Long examId);

    @Select("select ac_id from exam_depart_user_rel where member_id=#{userId}")
    Set<Long> getAllExamRelByCurrentUser(Long userId);

    @Insert("insert into exam_paper_rel values(#{examId},#{paperId},#{paperType})")
    void addCompetitionPaperRel(@Param("examId")Long examId, @Param("paperId") Long paperId,@Param("paperType") Integer paperType);

    @Select("Select * from exam e inner join exam_depart_paper_rel edp on e.id = edp.ac_id where edp.paper_id=#{paperId}")
    List<Exam> getEexmByPaperId(Long paperId);

    @Select("select * from exam_depart_paper_rel where ac_id=#{examId} and depart_id=#{departId}")
    List<ExamDepartPaperRel> findPaperByexamIdanddepartId(@Param("examId") Long examId, @Param("departId") Long departId);

    @Select("select * from exam_depart_user_rel where ac_id=#{examId} and depart_id=#{departId}")
    List<ExamDepartUserRel> findUserByByexamIdanddepartId(@Param("examId") Long examId, @Param("departId") Long departId);

    List<ExamStatisticsVO> getExamListByTypeAndStatus(ExamStatisticsDto dto);


    /**
     *  参加考试的人
     * @param examDTO
     * @return
     */
    List<Map> getExamingUserList(ExamStatisticsDto examDTO);
    List<Map> getSelfExamingUserList(ExamStatisticsDto examDTO);


    /**
     *  考试统计 竞答：1 和 其他类型的对比
     * @param examDTO
     * @return
     */
    List<Map<String,Object>> getCompetiPCountCreated(ExamStatisticsDto examDTO);
    List<Map<String,Object>> getCompetiPCountUsed(ExamStatisticsDto examDTO);


    /**
     *  考试统计 非竞答活动
     */
    List<Map<String,Object>> getNotCompetiPCountCreated(ExamStatisticsDto examDTO);
    List<Map<String,Object>> getStatisticsPaperCountUsed(ExamStatisticsDto examDTO);


    ///最新统计查询方法////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *   已月份分组 ，统计考试的count
     * @param examDTO
     * @return
     */
    List<Map<String,Object>> getStatisticsExamCountByTypeAndStatus(ExamStatisticsDto examDTO);
    /**
     *   考试 使用试卷的统计count
     * @param examDTO
     * @return
     */
    List<Map<String,Object>> getStatisticsExPaperCountByTypeAndStatus(ExamStatisticsDto examDTO);
    /**
     *   考试 参考人员的统计count
     * @param examDTO
     * @return
     */
    List<Map<String,Object>> getStatisticsExUserCountByTypeAndStatus(ExamStatisticsDto examDTO);

    /**
     *   已考试的维护统计 每次考试参数的人数，使用的试卷 总量
     * @param examDTO
     * @return
     */
    List<Map<String,Object>> getStatisticsExUserCountGroupExId(ExamStatisticsDto examDTO);

    /**
     *   活动试题统计，竞答和非竞答的统计
     * @param examDTO
     * @return
     */
    List<Map<String,Object>> getStatisticsExPQCountGroupExId(ExamStatisticsDto examDTO);
    List<Map<String,Object>> getNotCompetiExPQCountGroupExId(ExamStatisticsDto examDTO);


    /**
     *   已考试的维护统计 每次考试统计 试卷的Count
     * @param examDTO
     * @return
     */
    List<Map<String, Object>> getStatisticsExPaperCountGroupExId(ExamStatisticsDto examDTO);

    /**
     * 用户维度，统计
     * @param examDTO
     * @return
     */
    List<Map<String, Object>> getStatisticsExCountGroupUserId(ExamStatisticsDto examDTO);
    List<Map<String, Object>> getStatisticsQesCountGroupUserId(ExamStatisticsDto examDTO);

    List<Map<String, Object>> getStatisticsSelfExCountGroupUserId(ExamStatisticsDto examDTO);
    List<Map<String, Object>> getStatisticsSelfQesCountGroupUserId(ExamStatisticsDto examDTO);


    /**
     *  查询活动列表 通过活动的状态
     */
    List<Exam> getExamingListByTypeAndStatus(ExamStatisticsDto examDTO);
    List<ExamStatisticsVO> getExamAndUserByTypeAndStatus(ExamStatisticsDto examDTO);


    List<Map<String, Object>> getStaticExUserBystatusAndType(ExamStatisticsDto examDTO);

    List<DrawResult> getUserTrainTimeAndRank(String date);


    List<UserRelatedDto> isUserRelated(String inStr);
}
