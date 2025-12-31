package com.cloud.exam.model.exam;

import com.alibaba.fastjson.JSON;
import com.cloud.exam.utils.word.WordImportUtil;
import com.cloud.utils.excel.Excel;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author tongkesong
 * @since 2024-06-21
 */
@Data
public class QuestionExcel {
    @Excel(name = "题干")
    private String question;
    @Excel(name = "A")
    private String a;
    @Excel(name = "B")
    private String b;
    @Excel(name = "C")
    private String c;
    @Excel(name = "D")
    private String d;
    @Excel(name = "E")
    private String e;
    @Excel(name = "F")
    private String f;
    @Excel(name = "G")
    private String g;
    @Excel(name = "H")
    private String h;
    @Excel(name = "I")
    private String i;
    @Excel(name = "J")
    private String j;
    @Excel(name = "正确答案")
    private String answer;
    @Excel(name = "解析")
    private String analysis;
    @Excel(name = "难易程度")
    private String difficulty;
    @Excel(name = "知识点")
    private String knowledgePoints;
    @Excel(name = "判读类型")
    private String pdType;

    public boolean isOptionsBlank() {
        return StringUtils.isBlank(a) && StringUtils.isBlank(b) && StringUtils.isBlank(c)
                && StringUtils.isBlank(d) && StringUtils.isBlank(e) && StringUtils.isBlank(f)
                && StringUtils.isBlank(g) && StringUtils.isBlank(h) && StringUtils.isBlank(i) && StringUtils.isBlank(j);
    }

    public boolean validAnswer(String type) {
        if ("1".equals(type) || "2".equals(type)) {
            return answer.matches("^[A-J]{1,10}$");
        } else if ("3".equals(type)) {
            return answer.matches("^[对错]$");
        }
        return true;
    }

    public boolean validPdType() {
        if (StringUtils.isBlank(pdType)) {
            return true;
        }
        String[] pdTypes = {"可见光", "红外", "SAR", "其他"};
        for (String type : pdTypes) {
            if (type.equals(pdType)) {
                return true;
            }
        }
        return false;
    }

    public String convertQuestion() {
        return new QuestionMedia(question).toString();
    }

    public String convertAnswer(String type) {
        String tmp = answer;
        if ("1".equals(type) || "2".equals(type)) {
            tmp = String.join(",", answer.split(""));
        } else if ("3".equals(type)) {
            tmp = "错".equals(answer) ? "false" : "true";
        } else if ("4".equals(type)) {
            Map<String, String> map = new LinkedHashMap<>();
            String[] ans = answer.split("；");
            for (int k = 0; k < ans.length; k++) {
                map.put("edit" + (k + 1), ans[k]);
            }
            return JSON.toJSONString(map);
        }
        return new QuestionMedia(tmp).toString();
    }

    public String convertOptions(String type) {
        Map<String, Object> map = new LinkedHashMap<>();
        if ("1".equals(type) || "2".equals(type)) {
            if (StringUtils.isNotBlank(a)) {
                map.put("A", new QuestionMedia(a));
            }
            if (StringUtils.isNotBlank(b)) {
                map.put("B", new QuestionMedia(b));
            }
            if (StringUtils.isNotBlank(c)) {
                map.put("C", new QuestionMedia(c));
            }
            if (StringUtils.isNotBlank(d)) {
                map.put("D", new QuestionMedia(d));
            }
            if (StringUtils.isNotBlank(e)) {
                map.put("E", new QuestionMedia(e));
            }
            if (StringUtils.isNotBlank(f)) {
                map.put("F", new QuestionMedia(f));
            }
            if (StringUtils.isNotBlank(g)) {
                map.put("G", new QuestionMedia(g));
            }
            if (StringUtils.isNotBlank(h)) {
                map.put("H", new QuestionMedia(h));
            }
            if (StringUtils.isNotBlank(i)) {
                map.put("I", new QuestionMedia(i));
            }
            if (StringUtils.isNotBlank(j)) {
                map.put("J", new QuestionMedia(j));
            }
        } else if ("4".equals(type)) {
            String[] ans = answer.split("；");
            for (int k = 0; k < ans.length; k++) {
                map.put("edit" + (k + 1), "");
            }
        }
        return JSON.toJSONString(map);
    }

    public String convertAnalysis() {
        return new QuestionMedia(analysis == null ? "" : analysis).toString();
    }

    public Double convertDifficulty() {

        return WordImportUtil.convertDifficuty(difficulty);
    }
}
