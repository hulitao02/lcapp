package com.cloud.exam.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 学员的知识点平均成绩
 * </p>
 *
 * @author tongkesong
 * @since 2023-09-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ExamKpPersonAvgScore extends Model<ExamKpPersonAvgScore> {

  private static final long serialVersionUID = 1L;

  /**
   * 自增主键
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 学员id
   */
  private Long userId;

  /**
   * 知识点id
   */
  private String kpId;

  /**
   * 试卷id
   */
  private Long paperId;

  /**
   * 总分
   */
  private Long totalScore;

  /**
   * 试题总数
   */
  private Integer questionCount;

  /**
   * 平均分
   */
  private BigDecimal avgScore;

  /**
   * 创建时间
   */
  private Date createTime;

  /**
   * 更新时间
   */
  private Date updateTime;

  /**
   * 活动id
   */
  private Long acId;


  @Override
  protected Serializable pkVal() {
    return this.id;
  }

}
